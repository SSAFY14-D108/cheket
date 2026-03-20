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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        // 회차가 DB에 있나
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("회차를 찾을 수 없습니다: " + sessionId));
        // 민팅 완료됐나 (onChainSessionId가 있어야 온체인 티켓이 존재)
        if (session.getOnChainSessionId() == null) { // → onChainSessionId=0 → 민팅 완료 ✅
            throw new IllegalStateException("아직 민팅되지 않은 회차입니다");
        }
        // sessionSeatIds [1646, 1647, 1648]이 DB에 있나
        List<SessionSeat> seats = sessionSeatRepository.findAllById(sessionSeatIds);
        if (seats.size() != sessionSeatIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 좌석이 포함되어 있습니다");
        }
        // 각 좌석이 구매 가능한 상태인가
        for (SessionSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("이미 판매된 좌석입니다: sessionSeatId=" + seat.getId());
            }
            // → 1646: AVAILABLE, 1647: AVAILABLE, 1648: AVAILABLE
            if (seat.getOnChainTicketNftId() == null) {
                throw new IllegalStateException("온체인 티켓이 없는 좌석입니다: sessionSeatId=" + seat.getId());
            }
            // → 1646: nftId=0, 1647: nftId=1, 1648: nftId=2
        }

        // ========== ② 좌석 → PENDING_TX ==========
        // 블록체인 처리 중(~30초) 다른 사용자가 같은 좌석을 구매하지 못하도록
        for (SessionSeat seat : seats) {
            seat.setStatus(SeatStatus.PENDING_TX);
        }
        sessionSeatRepository.saveAll(seats);
        // DB 변화:
        // session_seats 테이블:
        // id=1646: AVAILABLE → PENDING_TX
        // id=1647: AVAILABLE → PENDING_TX
        // id=1648: AVAILABLE → PENDING_TX
        //
        // 이 상태면 다른 사용자가 같은 좌석으로 purchase 호출해도
        // ①번 검증에서 "이미 판매된 좌석" 에러 발생 → 중복 구매 방지

        // ========== ③ Transaction 레코드 생성 ==========
        // transaction 테이블에 1행 INSERT
        // status = PENDING → 아직 블록체인에 아무것도 안 보냄
        // amount = 0 → 온체인 가격 조회 후 @Async에서 업데이트
        Transaction transaction = Transaction.builder()
            .type(Transaction.TransactionType.PURCHASE)  // 거래 유형: 구매
            .amount(0L)               // 아직 온체인 가격 모름 → 나중에 업데이트
            .description("티켓 구매 대기")  // 초기 상태 메시지
            .txStatus(Transaction.TxStatus.PENDING)  // 아직 블록체인에 안 보냄
            .buyerId(userId)          // 구매자 ID
            .build();
        transactionRepository.save(transaction);  // DB INSERT

        log.info("[티켓 구매] PENDING 생성 — txId={}", transaction.getId());

        // ========== ④ @Async Worker에게 블록체인 처리 위임 ==========
        // 문제: @Transactional 안에서 @Async를 호출하면
        //       트랜잭션이 아직 커밋 안 된 상태에서 @Async 스레드가 실행됨
        //       → @Async 스레드가 Transaction을 못 찾음 (NoSuchElementException)
        // 해결: TransactionSynchronization.afterCommit()으로
        //       트랜잭션 커밋 완료 후에 @Async를 호출
        Long txId = transaction.getId();
        Long onChainSessionId = session.getOnChainSessionId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncWorker.processOnChainPurchase(
                    txId,                  // txId  → Transaction DB를 업데이트하려고 (PENDING → SUBMITTED → CONFIRMED)
                    userId,                // 구매자 ID → 사용자 지갑 Keystore를 찾으려고 (userId → User → Wallet → 개인키)
                    showId,                // 공연 ID
                    sessionId,             // 회차 ID
                    sessionSeatIds,        // 좌석 ID 목록 → DB에서 좌석 조회 → onChainTicketNftId 꺼내려고
                    onChainSessionId       // 온체인 회차 ID → purchaseTicket(buyer, ticketNftId, onChainSessionId) 호출하려고
                );
            }
        });

        // ========== ⑤ txId 즉시 반환 ==========
        // @Async 덕분에 여기까지 0.05초만에 도달
        // 클라이언트는 이 txId로 GET /api/v1/wallets/transactions/{txId} 폴링
        return transaction.getId();
    }
}
