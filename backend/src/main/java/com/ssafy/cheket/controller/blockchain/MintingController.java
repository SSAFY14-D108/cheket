package com.ssafy.cheket.controller.blockchain;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.repository.show.ShowRepository;
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

    // ========== 온체인 상태 조회 API ==========

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
}
