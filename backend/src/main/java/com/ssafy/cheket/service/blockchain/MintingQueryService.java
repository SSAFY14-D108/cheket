package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MintingQueryService {

    private final ShowMintingService showMintingService;
    private final BlockchainService blockchainService;
    private final ShowRepository showRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SessionRepository sessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    // ========== 민팅 ==========

    public void mintShow(Long showId) {
        showMintingService.mintShowNfts(showId);
    }

    public Map<String, Object> mintAllDraftShows() {
        List<Show> draftShows = showRepository.findByStatusAndReservationStartDateBetween(ShowStatus.DRAFT,
            LocalDateTime.MIN, LocalDateTime.MAX);

        log.info("[민팅] DRAFT 공연 전체 민팅 — {}건", draftShows.size());

        int success = 0;
        int failed = 0;

        for (Show show : draftShows) {
            try {
                showMintingService.mintShowNfts(show.getId());
                success++;
            } catch (Exception e) {
                log.error("[민팅] 공연 {} 민팅 실패", show.getId(), e);
                failed++;
            }
        }

        return Map.of("message", "전체 민팅 완료", "total", draftShows.size(), "success", success, "failed", failed);
    }

    // ========== showId 기반 온체인 통합 조회 ==========

    public Map<String, Object> getShowOnChainStatus(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다: " + showId));

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

        return result;
    }

    // ========== 온체인 개별 조회 ==========

    public Map<String, Object> getStakeholder(Long tokenId) {
        try {
            var result = blockchainService.getStakeholderNFT().getStakeholder(BigInteger.valueOf(tokenId)).send();

            Map<String, Object> data = new HashMap<>();
            data.put("tokenId", tokenId);
            data.put("wallet", result.component1());
            data.put("role", result.component2());
            data.put("shareBps", result.component3());
            data.put("eventNftId", result.component4());
            return data;
        } catch (Exception e) {
            throw new BlockchainException("StakeholderNFT 조회 실패: tokenId=" + tokenId + " — " + e.getMessage());
        }
    }

    public Map<String, Object> getEvent(Long eventId) {
        try {
            var info = blockchainService.getEventNFT().getEventInfo(BigInteger.valueOf(eventId)).send();
            var stakeholderIds = blockchainService.getEventNFT().getStakeholderTokenIds(BigInteger.valueOf(eventId))
                .send();
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
            return data;
        } catch (Exception e) {
            throw new BlockchainException("EventNFT 조회 실패: eventId=" + eventId + " — " + e.getMessage());
        }
    }

    public Map<String, Object> getSession(Long sessionId) {
        try {
            var result = blockchainService.getEventNFT().getSession(BigInteger.valueOf(sessionId)).send();

            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("eventId", result.component1());
            data.put("sessionTimestamp", result.component2());
            data.put("ticketSupply", result.component3());
            data.put("isFinalized", result.component4());
            return data;
        } catch (Exception e) {
            throw new BlockchainException("회차 조회 실패: sessionId=" + sessionId + " — " + e.getMessage());
        }
    }

    public Map<String, Object> getTicket(Long tokenId) {
        try {
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
            return data;
        } catch (Exception e) {
            throw new BlockchainException("TicketNFT 조회 실패: tokenId=" + tokenId + " — " + e.getMessage());
        }
    }

    public Map<String, Object> getTicketTotalSupply() {
        try {
            BigInteger totalSupply = blockchainService.getTicketNFT().totalSupply().send();
            return Map.of("totalSupply", totalSupply);
        } catch (Exception e) {
            throw new BlockchainException("TicketNFT totalSupply 조회 실패 — " + e.getMessage());
        }
    }

    public Map<String, Object> getRefundPolicy(Long eventId) {
        try {
            var result = blockchainService.getEventNFT().getRefundPolicies(BigInteger.valueOf(eventId)).send();

            Map<String, Object> data = new HashMap<>();
            data.put("eventId", eventId);
            data.put("daysArray", result.component1());
            data.put("rateBpsArray", result.component2());
            return data;
        } catch (Exception e) {
            throw new BlockchainException("환불 정책 조회 실패: eventId=" + eventId + " — " + e.getMessage());
        }
    }

    // ========== 유틸 ==========

    public Map<String, Object> getSeatOnChainMapping(Long sessionSeatId) {
        SessionSeat seat = sessionSeatRepository.findById(sessionSeatId)
            .orElseThrow(() -> new NotFoundException("좌석을 찾을 수 없습니다: " + sessionSeatId));

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
        return data;
    }

    public Map<String, Object> getShowSeatsMapping(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다: " + showId));

        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);
        String platformWallet = blockchainService.getPlatformWalletAddress();

        Map<String, Object> result = new HashMap<>();
        result.put("showId", showId);
        result.put("title", show.getTitle());

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
                    default -> {
                    }
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
        return result;
    }

    public Map<String, Object> getUserOnChainBalance(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));
        Wallet wallet = walletRepository.findById(user.getWalletId())
            .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다"));

        BigInteger onChainBalance = blockchainService.getSsfBalance(wallet.getAddress());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("walletAddress", wallet.getAddress());
        data.put("dbBalance", wallet.getCtkBalance());
        data.put("onChainBalance", onChainBalance);
        data.put("isMatch",
            onChainBalance.longValue() == (wallet.getCtkBalance() != null ? wallet.getCtkBalance() : 0));
        return data;
    }

    public Map<String, Object> getUserOwnedTickets(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다: " + userId));
        Wallet wallet = walletRepository.findById(user.getWalletId())
            .orElseThrow(() -> new NotFoundException("지갑을 찾을 수 없습니다"));

        String walletAddress = wallet.getAddress();
        BigInteger totalSupply;
        try {
            totalSupply = blockchainService.getTicketNFT().totalSupply().send();
        } catch (Exception e) {
            throw new BlockchainException("TicketNFT totalSupply 조회 실패 — " + e.getMessage());
        }

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
        return data;
    }

    public Map<String, Object> getSettlementDeposit(Long onChainSessionId) {
        try {
            BigInteger deposited = blockchainService.getSettlement()
                .sessionDeposits(BigInteger.valueOf(onChainSessionId)).send();

            Map<String, Object> data = new HashMap<>();
            data.put("onChainSessionId", onChainSessionId);
            data.put("depositedAmount", deposited);
            return data;
        } catch (Exception e) {
            throw new BlockchainException("Settlement 조회 실패: sessionId=" + onChainSessionId + " — " + e.getMessage());
        }
    }

    public Map<String, Object> getAvailableSeats(Long showId, int count) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다: " + showId));

        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

        Map<String, Object> result = new HashMap<>();
        result.put("showId", showId);
        result.put("showTitle", show.getTitle());
        result.put("showStatus", show.getStatus().name());

        List<Map<String, Object>> sessionList = new ArrayList<>();
        for (Session session : sessions) {
            List<SessionSeat> allSeats = sessionSeatRepository.findBySessionId(session.getId());
            List<SessionSeat> availableSeats = allSeats.stream().filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .collect(Collectors.toList());

            Collections.shuffle(availableSeats);
            List<SessionSeat> selected = availableSeats.subList(0, Math.min(count, availableSeats.size()));

            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("sessionId", session.getId());
            sessionData.put("totalAvailable", availableSeats.size());
            sessionData.put("selectedCount", selected.size());
            sessionData.put("selectedSeatIds", selected.stream().map(SessionSeat::getId).toList());
            sessionData.put("seats", selected.stream().map(seat -> {
                Map<String, Object> seatData = new HashMap<>();
                seatData.put("sessionSeatId", seat.getId());
                seatData.put("seatId", seat.getSeatId());
                seatData.put("onChainTicketNftId", seat.getOnChainTicketNftId());
                seatData.put("status", seat.getStatus().name());
                return seatData;
            }).toList());

            sessionList.add(sessionData);
        }

        result.put("sessions", sessionList);
        return result;
    }
}
