package com.ssafy.cheket.controller.blockchain;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.blockchain.BlockchainService;
import com.ssafy.cheket.service.blockchain.ShowMintingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MintingController — 블록체인 민팅 테스트용 API
 *
 * 스케줄러(새벽 3시)를 기다리지 않고 수동으로 민팅을 트리거할 수 있다. 개발/테스트 환경에서 사용. 운영 시에는 비활성화 권장.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/minting")
@Tag(name = "Minting (테스트)", description = "블록체인 민팅 테스트 API")
public class MintingController {

    private final ShowMintingService showMintingService;
    private final ShowRepository showRepository;
    private final BlockchainService blockchainService;
    private final StakeholderRepository stakeholderRepository;
    private final SessionRepository sessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    /**
     * DRAFT 상태인 공연 단건 민팅 (EventNFT + TicketNFT)
     */
    @PostMapping("/shows/{showId}")
    @Operation(summary = "공연 단건 민팅", description = "DRAFT 상태의 공연을 수동으로 민팅 (EventNFT + TicketNFT 발행)")
    public ResponseEntity<Map<String, Object>> mintShow(@PathVariable Long showId) {
        log.info("[민팅 API] 공연 단건 민팅 요청 — showId={}", showId);
        showMintingService.mintShowNfts(showId);
        return ResponseEntity.ok(Map.of("message", "민팅 완료", "showId", showId));
    }

    /**
     * DRAFT 상태인 공연 전체 민팅
     */
    @PostMapping("/shows/all")
    @Operation(summary = "DRAFT 공연 전체 민팅", description = "DRAFT 상태의 모든 공연을 순차적으로 민팅")
    public ResponseEntity<Map<String, Object>> mintAllDraftShows() {
        List<Show> draftShows = showRepository.findByStatusAndReservationStartDateBetween(ShowStatus.DRAFT,
            java.time.LocalDateTime.MIN, java.time.LocalDateTime.MAX);

        log.info("[민팅 API] DRAFT 공연 전체 민팅 요청 — {}건", draftShows.size());

        int success = 0;
        int failed = 0;

        for (Show show : draftShows) {
            try {
                showMintingService.mintShowNfts(show.getId());
                success++;
            } catch (Exception e) {
                log.error("[민팅 API] 공연 {} 민팅 실패", show.getId(), e);
                failed++;
            }
        }

        return ResponseEntity
            .ok(Map.of("message", "전체 민팅 완료", "total", draftShows.size(), "success", success, "failed", failed));
    }

    // ========== showId 기반 온체인 통합 조회 ==========

    /**
     * showId로 온체인 상태 전체 조회 DB에서 eventNftId, stakeholderNftId, onChainSessionId,
     * onChainTicketNftId를 찾아서 온체인 데이터를 한번에 반환
     */
    @GetMapping("/onchain/show/{showId}")
    @Operation(summary = "공연 온체인 상태 통합 조회", description = "showId로 StakeholderNFT + EventNFT + 회차 + TicketNFT 온체인 상태를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getShowOnChainStatus(@PathVariable Long showId) throws Exception {
        Show show = showRepository.findById(showId).orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다: " + showId));

        Map<String, Object> result = new HashMap<>();
        result.put("showId", showId);
        result.put("title", show.getTitle());
        result.put("dbStatus", show.getStatus());
        result.put("dbEventNftId", show.getEventNftId());

        // ① StakeholderNFT 온체인 조회
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(showId);
        List<Map<String, Object>> stakeholderList = new ArrayList<>();
        for (Stakeholder s : stakeholders) {
            Map<String, Object> sData = new HashMap<>();
            sData.put("dbId", s.getId());
            sData.put("dbRole", s.getRole());
            sData.put("dbShareBps", s.getShareBps());
            sData.put("dbStakeholderNftId", s.getStakeholderNftId());
            if (s.getStakeholderNftId() != null) {
                try {
                    var onChain = blockchainService.getStakeholderNFT()
                        .getStakeholder(BigInteger.valueOf(s.getStakeholderNftId())).send();
                    sData.put("onChainWallet", onChain.component1());
                    sData.put("onChainRole", onChain.component2());
                    sData.put("onChainShareBps", onChain.component3());
                } catch (Exception e) {
                    sData.put("onChainError", e.getMessage());
                }
            }
            stakeholderList.add(sData);
        }
        result.put("stakeholders", stakeholderList);

        // ② EventNFT 온체인 조회
        if (show.getEventNftId() != null) {
            try {
                BigInteger eventId = BigInteger.valueOf(show.getEventNftId());
                var info = blockchainService.getEventNFT().getEventInfo(eventId).send();
                var stkIds = blockchainService.getEventNFT().getStakeholderTokenIds(eventId).send();

                Map<String, Object> eventData = new HashMap<>();
                eventData.put("totalSupply", info.component2());
                eventData.put("maxPerWallet", info.component3());
                eventData.put("resaleCapBps", info.component4());
                eventData.put("bookingStartTime", info.component5());
                eventData.put("bookingEndTime", info.component6());
                eventData.put("isActive", info.component7());
                eventData.put("stakeholderTokenIds", stkIds);
                result.put("eventNft", eventData);

                // 환불 정책
                var refund = blockchainService.getEventNFT().getRefundPolicies(eventId).send();
                Map<String, Object> refundData = new HashMap<>();
                refundData.put("daysArray", refund.component1());
                refundData.put("rateBpsArray", refund.component2());
                result.put("refundPolicy", refundData);
            } catch (Exception e) {
                result.put("eventNftError", e.getMessage());
            }
        }

        // ③ 회차 + TicketNFT 온체인 조회
        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);
        List<Map<String, Object>> sessionList = new ArrayList<>();
        for (Session session : sessions) {
            Map<String, Object> sessData = new HashMap<>();
            sessData.put("dbSessionId", session.getId());
            sessData.put("dbOnChainSessionId", session.getOnChainSessionId());
            sessData.put("sessionDate", session.getSessionDate());

            // 회차 온체인 조회
            if (session.getOnChainSessionId() != null) {
                try {
                    var onChain = blockchainService.getEventNFT()
                        .getSession(BigInteger.valueOf(session.getOnChainSessionId())).send();
                    sessData.put("onChainTicketSupply", onChain.component3());
                    sessData.put("onChainIsFinalized", onChain.component4());
                } catch (Exception e) {
                    sessData.put("onChainError", e.getMessage());
                }
            }

            // 이 회차의 TicketNFT 샘플 조회 (처음 3개만)
            List<SessionSeat> seats = sessionSeatRepository.findBySessionId(session.getId());
            List<Map<String, Object>> ticketSamples = new ArrayList<>();
            int sampleCount = Math.min(3, seats.size());
            for (int i = 0; i < sampleCount; i++) {
                SessionSeat ss = seats.get(i);
                if (ss.getOnChainTicketNftId() != null) {
                    try {
                        var ticket = blockchainService.getTicketNFT()
                            .getTicket(BigInteger.valueOf(ss.getOnChainTicketNftId())).send();
                        String owner = blockchainService.getTicketNFT()
                            .ownerOf(BigInteger.valueOf(ss.getOnChainTicketNftId())).send();

                        Map<String, Object> tData = new HashMap<>();
                        tData.put("tokenId", ss.getOnChainTicketNftId());
                        tData.put("owner", owner);
                        tData.put("section", ticket.section);
                        tData.put("row", ticket.row);
                        tData.put("seat", ticket.seat);
                        tData.put("grade", ticket.grade);
                        tData.put("price", ticket.price);
                        tData.put("status", ticket.status);
                        ticketSamples.add(tData);
                    } catch (Exception e) {
                        ticketSamples.add(Map.of("tokenId", ss.getOnChainTicketNftId(), "error", e.getMessage()));
                    }
                }
            }
            sessData.put("ticketSamples", ticketSamples);
            sessData.put("totalSeats", seats.size());
            sessData.put("mintedSeats", seats.stream().filter(s -> s.getOnChainTicketNftId() != null).count());

            sessionList.add(sessData);
        }
        result.put("sessions", sessionList);

        return ResponseEntity.ok(result);
    }

    // ========== 온체인 상태 개별 조회 API ==========

    /**
     * StakeholderNFT 온체인 조회
     */
    @GetMapping("/onchain/stakeholder/{tokenId}")
    @Operation(summary = "StakeholderNFT 온체인 조회", description = "온체인에 기록된 이해관계자 정보 조회")
    public ResponseEntity<Map<String, Object>> getStakeholder(@PathVariable Long tokenId) throws Exception {
        var result = blockchainService.getStakeholderNFT().getStakeholder(BigInteger.valueOf(tokenId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("tokenId", tokenId);
        data.put("wallet", result.component1());
        data.put("role", result.component2());
        data.put("shareBps", result.component3());
        data.put("eventNftId", result.component4());
        return ResponseEntity.ok(data);
    }

    /**
     * EventNFT 온체인 조회
     */
    @GetMapping("/onchain/event/{eventId}")
    @Operation(summary = "EventNFT 온체인 조회", description = "온체인에 기록된 공연 정보 조회")
    public ResponseEntity<Map<String, Object>> getEvent(@PathVariable Long eventId) throws Exception {
        var info = blockchainService.getEventNFT().getEventInfo(BigInteger.valueOf(eventId)).send();

        var stakeholderIds = blockchainService.getEventNFT().getStakeholderTokenIds(BigInteger.valueOf(eventId)).send();

        var sessionIds = blockchainService.getEventNFT().getSessionIds(BigInteger.valueOf(eventId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("metadataCID", info.component1());
        data.put("totalSupply", info.component2());
        data.put("maxPerWallet", info.component3());
        data.put("resaleCapBps", info.component4());
        data.put("bookingStartTime", info.component5());
        data.put("bookingEndTime", info.component6());
        data.put("isActive", info.component7());
        data.put("stakeholderTokenIds", stakeholderIds);
        data.put("sessionIds", sessionIds);
        return ResponseEntity.ok(data);
    }

    /**
     * EventNFT 회차 온체인 조회
     */
    @GetMapping("/onchain/session/{sessionId}")
    @Operation(summary = "회차 온체인 조회", description = "온체인에 기록된 회차 정보 조회")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable Long sessionId) throws Exception {
        var result = blockchainService.getEventNFT().getSession(BigInteger.valueOf(sessionId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("sessionId", sessionId);
        data.put("eventId", result.component1());
        data.put("sessionTimestamp", result.component2());
        data.put("ticketSupply", result.component3());
        data.put("isFinalized", result.component4());
        return ResponseEntity.ok(data);
    }

    /**
     * TicketNFT 온체인 조회
     */
    @GetMapping("/onchain/ticket/{tokenId}")
    @Operation(summary = "TicketNFT 온체인 조회", description = "온체인에 기록된 티켓 정보 조회")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Long tokenId) throws Exception {
        var ticket = blockchainService.getTicketNFT().getTicket(BigInteger.valueOf(tokenId)).send();

        String owner = blockchainService.getTicketNFT().ownerOf(BigInteger.valueOf(tokenId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("tokenId", tokenId);
        data.put("owner", owner);
        data.put("eventId", ticket.eventId);
        data.put("sessionId", ticket.sessionId);
        data.put("section", ticket.section);
        data.put("row", ticket.row);
        data.put("seat", ticket.seat);
        data.put("grade", ticket.grade);
        data.put("price", ticket.price);
        data.put("status", ticket.status);
        data.put("mintedAt", ticket.mintedAt);
        return ResponseEntity.ok(data);
    }

    /**
     * TicketNFT 총 발행 수 조회
     */
    @GetMapping("/onchain/ticket/total-supply")
    @Operation(summary = "TicketNFT 총 발행 수", description = "온체인에 발행된 전체 TicketNFT 수 조회")
    public ResponseEntity<Map<String, Object>> getTicketTotalSupply() throws Exception {
        BigInteger totalSupply = blockchainService.getTicketNFT().totalSupply().send();
        return ResponseEntity.ok(Map.of("totalSupply", totalSupply));
    }

    /**
     * 환불 정책 온체인 조회
     */
    @GetMapping("/onchain/event/{eventId}/refund-policy")
    @Operation(summary = "환불 정책 온체인 조회", description = "온체인에 기록된 환불 정책 조회")
    public ResponseEntity<Map<String, Object>> getRefundPolicy(@PathVariable Long eventId) throws Exception {
        var result = blockchainService.getEventNFT().getRefundPolicies(BigInteger.valueOf(eventId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("daysArray", result.component1());
        data.put("rateBpsArray", result.component2());
        return ResponseEntity.ok(data);
    }

    // ========== 추가 유틸 API ==========

    /**
     * ① sessionSeatId → 온체인 티켓 매핑 조회
     * "이 좌석의 온체인 티켓 정보가 뭐야?"
     */
    @GetMapping("/onchain/seat/{sessionSeatId}")
    @Operation(summary = "좌석 → 온체인 티켓 매핑 조회",
        description = "sessionSeatId로 DB 좌석 정보 + 온체인 TicketNFT 정보를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getSeatOnChainMapping(
        @PathVariable Long sessionSeatId) throws Exception {
        SessionSeat seat = sessionSeatRepository.findById(sessionSeatId)
            .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + sessionSeatId));

        Map<String, Object> data = new HashMap<>();
        data.put("sessionSeatId", seat.getId());
        data.put("sessionId", seat.getSessionId());
        data.put("dbStatus", seat.getStatus());
        data.put("onChainTicketNftId", seat.getOnChainTicketNftId());

        if (seat.getOnChainTicketNftId() != null) {
            try {
                BigInteger tokenId = BigInteger.valueOf(seat.getOnChainTicketNftId());
                var ticket = blockchainService.getTicketNFT().getTicket(tokenId).send();
                String owner = blockchainService.getTicketNFT().ownerOf(tokenId).send();

                data.put("onChainOwner", owner);
                data.put("onChainSection", ticket.section);
                data.put("onChainRow", ticket.row);
                data.put("onChainSeat", ticket.seat);
                data.put("onChainGrade", ticket.grade);
                data.put("onChainPrice", ticket.price);
                data.put("onChainStatus", ticket.status);
            } catch (Exception e) {
                data.put("onChainError", e.getMessage());
            }
        }
        return ResponseEntity.ok(data);
    }

    /**
     * ② showId → 전체 좌석 + 온체인 매핑 + 판매 현황 조회
     * "이 공연의 좌석별 판매 현황이 어때?"
     */
    @GetMapping("/onchain/show/{showId}/seats")
    @Operation(summary = "공연 전체 좌석 + 온체인 매핑 조회",
        description = "showId로 전체 좌석의 DB 상태 + 온체인 owner를 조회 (판매 현황 파악)")
    public ResponseEntity<Map<String, Object>> getShowSeatsMapping(@PathVariable Long showId) throws Exception {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new RuntimeException("공연을 찾을 수 없습니다: " + showId));

        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

        Map<String, Object> result = new HashMap<>();
        result.put("showId", showId);
        result.put("title", show.getTitle());

        String platformWallet = blockchainService.getPlatformWalletAddress();
        List<Map<String, Object>> sessionList = new ArrayList<>();

        for (Session session : sessions) {
            Map<String, Object> sessData = new HashMap<>();
            sessData.put("sessionId", session.getId());
            sessData.put("sessionDate", session.getSessionDate());

            List<SessionSeat> seats = sessionSeatRepository.findBySessionId(session.getId());
            int available = 0;
            int pendingTx = 0;
            int sold = 0;

            List<Map<String, Object>> seatList = new ArrayList<>();
            for (SessionSeat seat : seats) {
                Map<String, Object> seatData = new HashMap<>();
                seatData.put("sessionSeatId", seat.getId());
                seatData.put("dbStatus", seat.getStatus());
                seatData.put("onChainTicketNftId", seat.getOnChainTicketNftId());

                // 온체인 owner 조회
                if (seat.getOnChainTicketNftId() != null) {
                    try {
                        String owner = blockchainService.getTicketNFT()
                            .ownerOf(BigInteger.valueOf(seat.getOnChainTicketNftId())).send();
                        seatData.put("onChainOwner", owner);
                        seatData.put("onChainSoldToUser", !owner.equalsIgnoreCase(platformWallet));
                    } catch (Exception e) {
                        seatData.put("onChainError", e.getMessage());
                    }
                }

                switch (seat.getStatus()) {
                    case AVAILABLE -> available++;
                    case PENDING_TX -> pendingTx++;
                    case SOLD -> sold++;
                    default -> { }
                }
                seatList.add(seatData);
            }

            sessData.put("seats", seatList);
            sessData.put("totalSeats", seats.size());
            sessData.put("available", available);
            sessData.put("pendingTx", pendingTx);
            sessData.put("sold", sold);
            sessionList.add(sessData);
        }
        result.put("sessions", sessionList);
        return ResponseEntity.ok(result);
    }

    /**
     * ③ 사용자 지갑 온체인 잔액 조회
     * "DB 잔액이랑 온체인 잔액이 맞나?"
     */
    @GetMapping("/onchain/wallet/{userId}/balance")
    @Operation(summary = "사용자 온체인 잔액 조회",
        description = "userId로 DB 잔액과 온체인 SSF 잔액을 비교 조회")
    public ResponseEntity<Map<String, Object>> getUserOnChainBalance(@PathVariable Long userId) throws Exception {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Wallet wallet = walletRepository.findById(user.getWalletId())
            .orElseThrow(() -> new RuntimeException("지갑을 찾을 수 없습니다"));

        BigInteger onChainBalance = blockchainService.getSsfBalance(wallet.getAddress());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("walletAddress", wallet.getAddress());
        data.put("dbBalance", wallet.getCtkBalance());
        data.put("onChainBalance", onChainBalance);
        data.put("isMatch", onChainBalance.longValue() == (wallet.getCtkBalance() != null ? wallet.getCtkBalance() : 0));
        return ResponseEntity.ok(data);
    }

    /**
     * ④ 사용자 보유 NFT 목록 조회
     * "이 사용자가 온체인에서 진짜 갖고 있는 티켓이 뭐야?"
     */
    @GetMapping("/onchain/wallet/{userId}/tickets")
    @Operation(summary = "사용자 보유 TicketNFT 조회",
        description = "userId의 지갑이 온체인에서 소유한 TicketNFT 목록 조회")
    public ResponseEntity<Map<String, Object>> getUserOwnedTickets(@PathVariable Long userId) throws Exception {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        Wallet wallet = walletRepository.findById(user.getWalletId())
            .orElseThrow(() -> new RuntimeException("지갑을 찾을 수 없습니다"));

        String walletAddress = wallet.getAddress();
        BigInteger totalSupply = blockchainService.getTicketNFT().totalSupply().send();

        List<Map<String, Object>> ownedTickets = new ArrayList<>();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(totalSupply) < 0; i = i.add(BigInteger.ONE)) {
            try {
                String owner = blockchainService.getTicketNFT().ownerOf(i).send();
                if (owner.equalsIgnoreCase(walletAddress)) {
                    var ticket = blockchainService.getTicketNFT().getTicket(i).send();
                    Map<String, Object> tData = new HashMap<>();
                    tData.put("tokenId", i);
                    tData.put("section", ticket.section);
                    tData.put("row", ticket.row);
                    tData.put("seat", ticket.seat);
                    tData.put("grade", ticket.grade);
                    tData.put("price", ticket.price);
                    tData.put("status", ticket.status);
                    ownedTickets.add(tData);
                }
            } catch (Exception e) {
                // 소각된 토큰 등 예외 무시
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("walletAddress", walletAddress);
        data.put("ownedCount", ownedTickets.size());
        data.put("tickets", ownedTickets);
        return ResponseEntity.ok(data);
    }

    /**
     * ⑤ Settlement 예치 현황 조회
     * "이 회차에 SSF 얼마나 예치됐어?"
     */
    @GetMapping("/onchain/settlement/{onChainSessionId}")
    @Operation(summary = "Settlement 예치 현황 조회",
        description = "온체인 회차 ID로 Settlement 컨트랙트에 예치된 SSF 금액 조회")
    public ResponseEntity<Map<String, Object>> getSettlementDeposit(
        @PathVariable Long onChainSessionId) throws Exception {
        BigInteger deposited = blockchainService.getSettlement()
            .sessionDeposits(BigInteger.valueOf(onChainSessionId)).send();

        Map<String, Object> data = new HashMap<>();
        data.put("onChainSessionId", onChainSessionId);
        data.put("depositedAmount", deposited);
        return ResponseEntity.ok(data);
    }
}
