package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.dto.ticket.response.GetUsedAndExpiredTicketResponse;
import com.ssafy.cheket.dto.queue.SeatAccessMeta;
import com.ssafy.cheket.dto.wallet.response.WalletBalanceResponse;
import com.ssafy.cheket.service.wallet.WalletService;
import com.ssafy.cheket.entity.show.RefundPolicy;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.ResaleStatus;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.GoneException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.entity.show.Seat;
import com.ssafy.cheket.repository.queue.QueueRepository;
import com.ssafy.cheket.entity.show.SeatGrade;
import com.ssafy.cheket.repository.show.RefundPolicyRepository;
import com.ssafy.cheket.repository.show.SeatGradeRepository;
import com.ssafy.cheket.repository.show.SeatRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.ticket.projection.UpcomingTicketProjection;
import com.ssafy.cheket.repository.ticket.projection.UsedAndExpiredTicketProjection;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
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
    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final QueueRepository queueRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final BlockchainAsyncWorker blockchainAsyncWorker;

    // ========== 티켓 구매 ==========

    /**
     * 티켓 구매 — 검증 + PENDING 생성 + 즉시 응답 블록체인 처리는 BlockchainAsyncWorker가 별도 스레드에서 담당
     */
    @Override
    @Transactional
    public Long purchaseTickets(Long userId, Long showId, Long sessionId, String seatAccessToken,
        List<Long> sessionSeatIds) {
        log.info("[티켓 구매] 요청 — userId={}, showId={}, sessionId={}, 좌석수={}", userId, showId, sessionId,
            sessionSeatIds.size());

        // 대기열 검증 (테스트용: "test" 토큰이면 스킵)
        if (!"test".equals(seatAccessToken)) {
            validateSeatAccessToken(userId, showId, sessionId, seatAccessToken);
        }

        // ① 유효성 검증
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다: " + sessionId));

        if (!session.getShowId().equals(showId)) {
            throw new NotFoundException("공연 정보와 회차 정보가 일치하지 않습니다.");
        }

        if (session.getOnChainSessionId() == null) {
            throw new ConflictException("아직 민팅되지 않은 회차입니다");
        }

        List<SessionSeat> seats = sessionSeatRepository.findAllById(sessionSeatIds);
        if (seats.size() != sessionSeatIds.size()) {
            throw new NotFoundException("존재하지 않는 좌석이 포함되어 있습니다");
        }

        for (SessionSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE && seat.getStatus() != SeatStatus.HELD) {
                throw new ConflictException("이미 판매된 좌석입니다: sessionSeatId=" + seat.getId());
            }
            if (seat.getOnChainTicketNftId() == null) {
                throw new NotFoundException("온체인 티켓이 없는 좌석입니다: sessionSeatId=" + seat.getId());
            }
        }
        // SSF 잔액 사전 검증 (온체인 balanceOf 직접 조회)
        int totalPrice = 0;
        for (SessionSeat seat : seats) {
            Seat seatInfo = seatRepository.findById(seat.getSeatId())
                .orElseThrow(() -> new NotFoundException("좌석 정보를 찾을 수 없습니다."));
            SeatGrade grade = seatGradeRepository.findByShowIdAndSectionId(showId, seatInfo.getSectionId())
                .orElseThrow(() -> new NotFoundException("좌석 등급을 찾을 수 없습니다."));
            totalPrice += grade.getPrice();
        }
        WalletBalanceResponse balanceResponse = walletService.refreshBalance(userId, "ROLE_USER");
        int currentBalance = balanceResponse.balance() != null ? balanceResponse.balance() : 0;
        if (currentBalance < totalPrice) {
            throw new ConflictException("SSF 잔액이 부족합니다. (보유: " + currentBalance + " SSF, 필요: " + totalPrice + " SSF)");
        }
        // 회차별 구매 한도 확인 (온체인 walletTicketCount와 동일 기준)
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다."));
        long currentCount = ticketRepository.countByUserIdAndSessionId(userId, sessionId);
        if (currentCount + seats.size() > show.getPurchaseLimit()) {
            throw new ConflictException("구매 한도를 초과합니다. (현재: " + currentCount + "매, 요청: " + seats.size() + "매, 한도: "
                + show.getPurchaseLimit() + "매)");
        }

        // ② 좌석 → PENDING_TX
        for (SessionSeat seat : seats) {
            seat.setStatus(SeatStatus.PENDING_TX);
        }
        sessionSeatRepository.saveAll(seats);

        // 구매 시작 후 seatAccessToken 재사용 방지
        queueRepository.deleteSeatAccessToken(seatAccessToken);

        // ③ Transaction PENDING 생성
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.PURCHASE).amount(0L)
            .description("요청 접수 — 티켓 구매 대기").txStatus(Transaction.TxStatus.PENDING).buyerId(userId).build();
        transactionRepository.save(transaction);

        log.info("[티켓 구매] PENDING 생성 — txId={}", transaction.getId());

        // ④ afterCommit에서 @Async 시작
        Long txId = transaction.getId();
        Long onChainSessionId = session.getOnChainSessionId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainPurchase(txId, userId, showId, sessionId, sessionSeatIds,
                    onChainSessionId);
            }
        });

        return transaction.getId();
    }

    private void validateSeatAccessToken(Long userId, Long showId, Long sessionId, String seatAccessToken) {
        SeatAccessMeta meta = queueRepository.findSeatAccessTokenMeta(seatAccessToken);

        if (meta == null) {
            throw new GoneException("좌석 선택 가능 시간이 만료되었습니다.");
        }
        if (!meta.getUserId().equals(userId)) {
            throw new ForbiddenException("seatAccessToken 소유자와 요청 사용자가 일치하지 않습니다.");
        }
        if (!meta.getShowId().equals(showId) || !meta.getSessionId().equals(sessionId)) {
            throw new ConflictException("요청 정보와 seatAccessToken 정보가 일치하지 않습니다.");
        }
    }

    // ========== 티켓 환불 ==========

    /**
     * 티켓 환불 — 검증 + PENDING 생성 + 즉시 응답 블록체인 처리는 BlockchainAsyncWorker가 별도 스레드에서 담당
     */
    @Override
    @Transactional
    public Long refundTicket(Long userId, Long ticketId) {
        log.info("[티켓 환불] 요청 — userId={}, ticketId={}", userId, ticketId);

        // ① 유효성 검증
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("존재하지 않는 티켓입니다."));

        if (!ticket.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 소유 티켓만 환불할 수 있습니다.");
        }

        if (ticket.getResaleStatus() == ResaleStatus.LISTED) {
            throw new ConflictException("리세일 등록된 티켓은 환불할 수 없습니다.");
        }

        if (ticket.getResaleStatus() == ResaleStatus.USED || ticket.getResaleStatus() == ResaleStatus.EXPIRED) {
            throw new ConflictException("이미 사용되었거나 만료된 티켓입니다.");
        }

        if (ticket.getTicketNftId() == null) {
            throw new ConflictException("온체인 티켓이 없는 좌석입니다.");
        }

        SessionSeat sessionSeat = sessionSeatRepository.findById(ticket.getSessionSeatId())
            .orElseThrow(() -> new NotFoundException("좌석 정보를 찾을 수 없습니다."));
        if (sessionSeat.getStatus() != SeatStatus.SOLD) {
            throw new ConflictException("환불 가능한 좌석 상태가 아닙니다.");
        }

        Session session = sessionRepository.findById(sessionSeat.getSessionId())
            .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));

        if (session.getOnChainSessionId() == null) {
            throw new ConflictException("아직 민팅되지 않은 회차입니다.");
        }

        // ② 환불 정책 검증
        List<RefundPolicy> refundPolicies = refundPolicyRepository
            .findByShowIdOrderByDaysRemainingDesc(session.getShowId());
        if (refundPolicies.isEmpty()) {
            throw new ConflictException("환불 정책이 등록되지 않은 공연입니다.");
        }

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long remain = ChronoUnit.DAYS.between(now, session.getSessionDate().toLocalDate());

        RefundPolicy targetPolicy = null;
        for (RefundPolicy policy : refundPolicies) {
            if (policy.getDaysRemaining() <= remain) {
                targetPolicy = policy;
                break;
            }
        }

        if (targetPolicy == null) {
            throw new ConflictException("환불 가능한 기간이 아닙니다.");
        }

        // ③ 좌석 → PENDING_TX
        sessionSeat.setStatus(SeatStatus.PENDING_TX);
        sessionSeatRepository.save(sessionSeat);

        // ④ Transaction PENDING 생성 (환불 금액은 온체인에서 결정)
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.REFUND).amount(0L).description(
            "요청 접수 — 티켓 환불 대기 (ticketId=%d, userId=%d, nftId=%d)".formatted(ticketId, userId, ticket.getTicketNftId()))
            .txStatus(Transaction.TxStatus.PENDING).buyerId(userId).build();
        transactionRepository.save(transaction);

        log.info("[티켓 환불] PENDING 생성 — txId={}", transaction.getId());

        // ⑤ afterCommit에서 @Async 시작
        Long txId = transaction.getId();
        Long onChainSessionId = session.getOnChainSessionId();
        Long onChainTicketNftId = ticket.getTicketNftId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainRefund(txId, userId, ticketId, onChainSessionId,
                    onChainTicketNftId);
            }
        });

        return txId;
    }

    // 보관 목록 조회
    @Override
    public List<GetUpcomingTicketResponse> getUpcomingTickets(Long userId) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<UpcomingTicketProjection> tickets = ticketRepository.findUpcomingAvailableAndListedTicketsByUserId(userId,
            now);

        return tickets.stream()
            .filter(ticket -> ticket.getStatus() != ResaleStatus.LISTED || ticket.getResalePrice() != null)
            .map(ticket -> new GetUpcomingTicketResponse(ticket.getTicketId(), ticket.getNumbering(),
                ticket.getPosterUrl(),
                new GetUpcomingTicketResponse.ShowInfo(ticket.getShowId(), ticket.getShowName(),
                    ticket.getSessionDate(), ticket.getVenueName()),
                ticket.getPrice(), ticket.getSeatId(), ticket.getSectionName(), ticket.getSeatNo(), ticket.getGrade(),
                ticket.getStatus(), ticket.getMetadataIpfsCid(),
                (ticket.getResalePrice() == null) ? 0 : ticket.getResalePrice()))
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
            .description("요청 접수 — 티켓 양도 대기").txStatus(Transaction.TxStatus.PENDING).sellerId(senderUserId) // 보내는 사람
            .buyerId(receiver.getId()) // 받는 사람
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

    /**
     * 리세일 등록 — 보유 티켓을 리세일 마켓에 등록 Escrow.createDeal()로 NFT를 Escrow에 예치 원가 이하 가격은
     * 온체인(Escrow)에서 강제 검증 deadline은 공연 시작 시각으로 서버가 자동 설정
     */
    @Override
    @Transactional
    public Long createResale(Long userId, Long ticketId, int resalePrice) {
        log.info("[리세일 등록] 요청 — userId={}, ticketId={}, resalePrice={}", userId, ticketId, resalePrice);

        // ① 티켓 소유자 확인
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("존재하지 않는 티켓입니다."));

        if (!ticket.getUserId().equals(userId)) {
            throw new ForbiddenException("본인 소유 티켓만 등록할 수 있습니다.");
        }

        if (ticket.getResaleStatus() != ResaleStatus.AVAILABLE) {
            throw new ConflictException("리세일 등록할 수 없는 상태입니다: " + ticket.getResaleStatus());
        }

        if (ticket.getTicketNftId() == null) {
            throw new ConflictException("온체인 티켓이 없는 좌석입니다.");
        }

        // ② 원가 조회 (SessionSeat → Seat → Section → SeatGrade)
        SessionSeat sessionSeat = sessionSeatRepository.findById(ticket.getSessionSeatId())
            .orElseThrow(() -> new NotFoundException("좌석 정보를 찾을 수 없습니다."));

        Seat seat = seatRepository.findById(sessionSeat.getSeatId())
            .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다."));

        Session session = sessionRepository.findById(sessionSeat.getSessionId())
            .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));

        Show show = showRepository.findById(session.getShowId())
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다."));

        // SeatGrade에서 원가 조회 (showId + sectionId)
        SeatGrade seatGrade = seatGradeRepository.findByShowIdAndSectionId(show.getId(), seat.getSectionId())
            .orElseThrow(() -> new NotFoundException("좌석 등급을 찾을 수 없습니다."));

        // 원가 이하인지 사전 검증 (온체인에서도 검증하지만 불필요한 TX 방지)
        if (resalePrice > seatGrade.getPrice()) {
            throw new ConflictException("원가(" + seatGrade.getPrice() + " SSF)를 초과하는 가격입니다.");
        }

        // ③ deadline = 공연 시작 시각
        LocalDateTime deadline = session.getSessionStartTime();

        if (deadline.isBefore(LocalDateTime.now())) {
            throw new ConflictException("이미 시작된 공연의 티켓은 리세일 등록할 수 없습니다.");
        }

        // ④ Ticket.resaleStatus → LISTED
        ticket.setResaleStatus(ResaleStatus.LISTED);
        ticketRepository.save(ticket);

        // ⑤ Transaction PENDING 생성
        Transaction transaction = Transaction.builder().type(Transaction.TransactionType.RESALE_CREATE)
            .amount((long) resalePrice).description("요청 접수 — 리세일 등록 대기").txStatus(Transaction.TxStatus.PENDING)
            .sellerId(userId) // 등록하는
                              // 사람
                              // =
                              // 판매자
            .build();
        transactionRepository.save(transaction);

        log.info("[리세일 등록] PENDING 생성 — txId={}, price={}, deadline={}", transaction.getId(), resalePrice, deadline);

        // ⑥ afterCommit → @Async
        Long txId = transaction.getId();
        Long onChainTicketNftId = ticket.getTicketNftId();
        long deadlineUnix = deadline.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainResaleCreate(txId, userId, ticketId, onChainTicketNftId,
                    resalePrice, deadlineUnix, seatGrade.getPrice());
            }
        });

        return txId;
    }

}
