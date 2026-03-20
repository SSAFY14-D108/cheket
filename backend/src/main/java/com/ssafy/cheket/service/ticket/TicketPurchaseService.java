package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TicketPurchaseService — 티켓 구매 1단계 (동기, 즉시 응답)
 *
 * [역할] ① 유효성 검증 (좌석 AVAILABLE 확인) ② 좌석 → PENDING_TX (다른 사용자 구매 방지) ③
 * Transaction 레코드 생성 (transaction 테이블에 1행 INSERT) ④ @Async Worker에게 블록체인 처리 위임
 * ⑤ txId 즉시 반환 (0.05초)
 *
 * 블록체인 처리(느린 작업)는 TicketPurchaseAsyncWorker가 별도 스레드에서 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPurchaseService {

    private final SessionRepository sessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final TransactionRepository transactionRepository;
    private final TicketPurchaseAsyncWorker asyncWorker;

    /**
     * 티켓 구매 요청 — 검증 + PENDING 생성 + 즉시 응답
     *
     * @param userId
     *            JWT에서 추출한 사용자 ID
     * @param showId
     *            공연 ID
     * @param sessionId
     *            회차 ID
     * @param sessionSeatIds
     *            구매할 좌석 ID 목록
     * @return txId (클라이언트가 이 ID로 상태 폴링)
     */
    @Transactional
    public Long purchaseTickets(Long userId, Long showId, Long sessionId, List<Long> sessionSeatIds) {
        log.info("[티켓 구매] 요청 — userId={}, showId={}, sessionId={}, 좌석수={}", userId, showId, sessionId,
            sessionSeatIds.size());

        // ========== ① 유효성 검증 ==========
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다: " + sessionId));

        if (session.getOnChainSessionId() == null) {
            throw new IllegalStateException("아직 민팅되지 않은 회차입니다");
        }

        List<SessionSeat> seats = sessionSeatRepository.findAllById(sessionSeatIds);
        if (seats.size() != sessionSeatIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 좌석이 포함되어 있습니다");
        }

        for (SessionSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("이미 판매된 좌석입니다: sessionSeatId=" + seat.getId());
            }
            if (seat.getOnChainTicketNftId() == null) {
                throw new IllegalStateException("온체인 티켓이 없는 좌석입니다: sessionSeatId=" + seat.getId());
            }
        }

        // ========== ② 좌석 → PENDING_TX ==========
        // 블록체인 처리 중(~30초) 다른 사용자가 같은 좌석을 구매하지 못하도록
        for (SessionSeat seat : seats) {
            seat.setStatus(SeatStatus.PENDING_TX);
        }
        sessionSeatRepository.saveAll(seats);

        // ========== ③ Transaction 레코드 생성 ==========
        // transaction 테이블에 1행 INSERT
        // status = PENDING → 아직 블록체인에 아무것도 안 보냄
        // amount = 0 → 온체인 가격 조회 후 @Async에서 업데이트
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.PURCHASE).amount(0L)
            .description("티켓 구매 대기").txStatus(Transaction.TxStatus.PENDING).buyerId(userId).build();
        transactionRepository.save(transaction);

        log.info("[티켓 구매] PENDING 생성 — txId={}", transaction.getId());

        // ========== ④ @Async Worker에게 블록체인 처리 위임 ==========
        // 별도 클래스(TicketPurchaseAsyncWorker)를 호출해야 @Async가 동작
        // 같은 클래스 내부 호출이면 Spring 프록시가 @Async를 무시함
        asyncWorker.processOnChainPurchase(transaction.getId(), userId, showId, sessionId, sessionSeatIds,
            session.getOnChainSessionId());

        // ========== ⑤ txId 즉시 반환 ==========
        // @Async 덕분에 여기까지 0.05초만에 도달
        // 클라이언트는 이 txId로 GET /api/v1/wallets/transactions/{txId} 폴링
        return transaction.getId();
    }
}
