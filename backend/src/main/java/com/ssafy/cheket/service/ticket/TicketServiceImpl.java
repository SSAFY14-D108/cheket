package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.dto.ticket.response.GetUsedAndExpiredTicketResponse;
import com.ssafy.cheket.entity.show.RefundPolicy;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.ResaleStatus;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.RefundPolicyRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.ticket.projection.UpcomingTicketProjection;
import com.ssafy.cheket.repository.ticket.projection.UsedAndExpiredTicketProjection;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.blockchain.BlockchainAsyncWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final SessionRepository sessionRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final ShowRepository showRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BlockchainAsyncWorker blockchainAsyncWorker;

    // 티켓 환불
    @Override
    public void refundTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));

        if (ticket.getResaleStatus().equals(ResaleStatus.EXPIRED)) {
            throw new NotFoundException("유효하지 않은 티켓입니다.");
        }

        SessionSeat sessionSeat = sessionSeatRepository.findById(ticket.getSessionSeatId())
            .orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));
        Session session = sessionRepository.findById(sessionSeat.getSessionId())
            .orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));
        List<RefundPolicy> refundPolicies = refundPolicyRepository
            .findByShowIdOrderByDaysRemainingDesc(session.getShowId());

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long remain = ChronoUnit.DAYS.between(now, session.getSessionDate());
        for (RefundPolicy policy : refundPolicies) {
            if (policy.getDaysRemaining() > remain) {
                continue;
            }

            // TODO: 환불 로직 작성
            // 1. 티켓의 소유자를 현재 소유자 -> 서버측으로 변경 시도(실패 시 에러)
            // 2. 소유자 변경 완료 시 원래 소유자의 지갑으로 환불 금액 만큼의 가격 환불
            // 3. 해당 NFT의 소유자를 서버 운영자 측으로 변경
            // 4. DB에서 Ticket 삭제
            // 5. SessionSeat의 STATUS를 SOLD -> AVAILABLE로 변경
            // 6. 모든 로직을 완료하면 return 해줘야 다음 로직을 이행하지 않음.
            return;
        }
    }

    // 보관 목록 조회
    @Override
    public List<GetUpcomingTicketResponse> getUpcomingTickets(Long userId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<UpcomingTicketProjection> tickets = ticketRepository.findUpcomingAvailableAndListedTicketsByUserId(userId,
            now);

        return tickets.stream()
            .map(ticket -> new GetUpcomingTicketResponse(ticket.getTicketId(), ticket.getNumbering(),
                ticket.getPosterUrl(),
                new GetUpcomingTicketResponse.ShowInfo(ticket.getShowId(), ticket.getShowName(),
                    ticket.getSessionDate(), ticket.getVenueName()),
                ticket.getPrice(), ticket.getSeatId(), ticket.getSectionName(), ticket.getSeatNo(), ticket.getGrade(),
                ticket.getStatus(), ticket.getMetadataIpfsCid(), ticket.getResalePrice()))
            .toList();
    }

    // 관람 완료 / 만료 목록 조회
    @Override
    public List<GetUsedAndExpiredTicketResponse> getUsedAndExpiredTickets(Long userId) {
        List<UsedAndExpiredTicketProjection> tickets = ticketRepository.findUsedAndExpiredTicketsByUserId(userId);

        return tickets.stream()
            .map(ticket -> new GetUsedAndExpiredTicketResponse(ticket.getTicketId(), ticket.getNumbering(),
                ticket.getPosterUrl(),
                new GetUsedAndExpiredTicketResponse.ShowInfo(ticket.getShowId(), ticket.getShowName(),
                    ticket.getSessionDate(), ticket.getVenueName(), ticket.getEffect()),
                ticket.getSeatId(), ticket.getSectionName(), ticket.getSeatNo(), ticket.getGrade()))
            .toList();
    }

    /**
     * 지정 양도 — 1단계 (동기, 즉시 응답)
     *
     * 구매와 동일한 비동기 패턴: 검증 → Transaction PENDING → afterCommit에서 @Async 시작 → txId 반환
     *
     * [왜 구매보다 간단?] - approve 불필요 (SSF 이동 없음, 무료) - Marketplace.directTransfer() 1
     * TX로 끝
     */
    @Override
    @Transactional
    public Long transferTicket(Long senderUserId, Long ticketId, String receiverPhoneNumber) {
        log.info("[양도] 요청 — senderUserId={}, ticketId={}, receiver={}", senderUserId, ticketId, receiverPhoneNumber);

        // ========== ① 보내는 사람이 이 티켓 주인 맞나? ==========
        // DB에서 Ticket 조회 → userId가 요청한 사용자와 일치하는지
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("존재하지 않는 티켓입니다."));

        if (!ticket.getUserId().equals(senderUserId)) {
            // 다른 사람의 티켓을 양도하려는 시도 차단
            throw new ForbiddenException("본인 소유의 티켓만 양도할 수 있습니다.");
        }

        // 티켓에 연결된 좌석의 온체인 NFT ID 확인
        // 이게 있어야 Marketplace.directTransfer()에 넘길 수 있음
        SessionSeat seat = sessionSeatRepository.findById(ticket.getSessionSeatId())
            .orElseThrow(() -> new NotFoundException("좌석 정보를 찾을 수 없습니다."));

        if (seat.getOnChainTicketNftId() == null) {
            throw new ConflictException("온체인 티켓이 없는 좌석입니다.");
        }

        // ========== ② 받는 사람 전화번호 → User 조회 ==========
        // Custodial: 사용자는 지갑 주소를 모르고, 전화번호로 식별
        // 서버가 전화번호 → User → Wallet → 지갑 주소로 변환
        User receiver = userRepository.findByPhoneNumber(receiverPhoneNumber)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        // 자기 자신에게 양도 방지
        if (receiver.getId().equals(senderUserId)) {
            throw new ConflictException("본인에게는 양도할 수 없습니다.");
        }

        // ========== ③ 받는 사람 maxPerWallet 초과 확인 ==========
        // 컨트랙트에서도 검증하지만, 불필요한 TX를 막기 위해 사전 필터링
        // "이 공연에 대해 받는 사람이 이미 몇 장 갖고 있나?"
        Session session = sessionRepository.findById(seat.getSessionId())
            .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));
        Show show = showRepository.findById(session.getShowId())
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다."));

        long receiverTicketCount = ticketRepository.countByUserIdAndShowId(receiver.getId(), show.getId());
        if (receiverTicketCount >= show.getPurchaseLimit()) {
            throw new ConflictException("받는 사람의 구매 한도(" + show.getPurchaseLimit() + "매)를 초과합니다.");
        }

        // ========== ④ Transaction PENDING 생성 ==========
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.TRANSFER) // 양도 타입
            .amount(0L) // 무료 양도
            .description("양도 처리 중").txStatus(Transaction.TxStatus.PENDING).buyerId(senderUserId) // 보내는 사람 기록
            .build();
        transactionRepository.save(transaction);

        log.info("[양도] PENDING 생성 — txId={}", transaction.getId());

        // ========== ⑤ afterCommit에서 @Async 시작 ==========
        // 왜 afterCommit?
        // @Transactional이 return할 때 커밋됨
        // 커밋 전에 @Async가 시작되면 Transaction을 DB에서 못 찾음
        // afterCommit: 커밋 완료 후에 @Async 시작 → Transaction 확실히 있음
        Long txId = transaction.getId();
        Long receiverUserId = receiver.getId();
        Long onChainTicketNftId = seat.getOnChainTicketNftId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainTransfer(txId, senderUserId, receiverUserId, ticketId,
                    onChainTicketNftId);
            }
        });

        // txId 즉시 반환 → 앱이 이걸로 폴링 시작
        return txId;
    }

}
