package com.ssafy.cheket.controller.blockchain;

import com.ssafy.cheket.service.blockchain.MintingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MintingController — 블록체인 민팅 테스트 + 온체인 조회 API
 *
 * 비즈니스 로직은 MintingQueryService에 위임
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/minting")
@Tag(name = "Minting (테스트)", description = "블록체인 민팅 테스트 API")
public class MintingController {

    private final MintingQueryService mintingQueryService;

    // ========== 민팅 ==========

    @PostMapping("/shows/{showId}")
    @Operation(summary = "공연 단건 민팅", description = "DRAFT 상태의 공연을 수동으로 민팅 (EventNFT + TicketNFT 발행)")
    public ResponseEntity<Map<String, Object>> mintShow(@PathVariable Long showId) {
        log.info("[민팅 API] 공연 단건 민팅 요청 — showId={}", showId);
        mintingQueryService.mintShow(showId);
        return ResponseEntity.ok(Map.of("message", "민팅 완료", "showId", showId));
    }

    @PostMapping("/shows/all")
    @Operation(summary = "DRAFT 공연 전체 민팅", description = "DRAFT 상태의 모든 공연을 순차적으로 민팅")
    public ResponseEntity<Map<String, Object>> mintAllDraftShows() {
        return ResponseEntity.ok(mintingQueryService.mintAllDraftShows());
    }

    // ========== showId 기반 온체인 통합 조회 ==========

    @GetMapping("/onchain/show/{showId}")
    @Operation(summary = "공연 온체인 상태 통합 조회", description = "showId로 StakeholderNFT + EventNFT + 회차 + TicketNFT 온체인 상태를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getShowOnChainStatus(@PathVariable Long showId) {
        return ResponseEntity.ok(mintingQueryService.getShowOnChainStatus(showId));
    }

    // ========== 온체인 상태 개별 조회 ==========

    @GetMapping("/onchain/stakeholder/{tokenId}")
    @Operation(summary = "StakeholderNFT 온체인 조회", description = "StakeholderNFT의 tokenId (stakeholder.stakeholder_nft_id)로 온체인 정보 조회")
    public ResponseEntity<Map<String, Object>> getStakeholder(@PathVariable Long tokenId) {
        return ResponseEntity.ok(mintingQueryService.getStakeholder(tokenId));
    }

    @GetMapping("/onchain/event/{eventId}")
    @Operation(summary = "EventNFT 온체인 조회", description = "EventNFT의 eventId (shows.event_nft_id)로 온체인 정보 조회")
    public ResponseEntity<Map<String, Object>> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(mintingQueryService.getEvent(eventId));
    }

    @GetMapping("/onchain/session/{sessionId}")
    @Operation(summary = "회차 온체인 조회", description = "EventNFT 내 sessionId (sessions.on_chain_session_id)로 온체인 정보 조회")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(mintingQueryService.getSession(sessionId));
    }

    @GetMapping("/onchain/ticket/{tokenId}")
    @Operation(summary = "TicketNFT 온체인 조회", description = "TicketNFT의 tokenId (session_seats.on_chain_ticket_nft_id)로 온체인 정보 조회")
    public ResponseEntity<Map<String, Object>> getTicket(@PathVariable Long tokenId) {
        return ResponseEntity.ok(mintingQueryService.getTicket(tokenId));
    }

    @GetMapping("/onchain/ticket/total-supply")
    @Operation(summary = "TicketNFT 총 발행 수", description = "온체인에 발행된 전체 TicketNFT 수 조회")
    public ResponseEntity<Map<String, Object>> getTicketTotalSupply() {
        return ResponseEntity.ok(mintingQueryService.getTicketTotalSupply());
    }

    @GetMapping("/onchain/event/{eventId}/refund-policy")
    @Operation(summary = "환불 정책 온체인 조회", description = "온체인에 기록된 환불 정책 조회")
    public ResponseEntity<Map<String, Object>> getRefundPolicy(@PathVariable Long eventId) {
        return ResponseEntity.ok(mintingQueryService.getRefundPolicy(eventId));
    }

    // ========== 유틸 ==========

    @GetMapping("/onchain/seat/{sessionSeatId}")
    @Operation(summary = "좌석 → 온체인 티켓 매핑 조회", description = "sessionSeatId로 DB 좌석 정보 + 온체인 TicketNFT 정보를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getSeatOnChainMapping(@PathVariable Long sessionSeatId) {
        return ResponseEntity.ok(mintingQueryService.getSeatOnChainMapping(sessionSeatId));
    }

    @GetMapping("/onchain/show/{showId}/seats")
    @Operation(summary = "공연 전체 좌석 + 온체인 매핑 조회", description = "showId로 전체 좌석의 DB 상태 + 온체인 owner를 조회 (판매 현황 파악)")
    public ResponseEntity<Map<String, Object>> getShowSeatsMapping(@PathVariable Long showId) {
        return ResponseEntity.ok(mintingQueryService.getShowSeatsMapping(showId));
    }

    @GetMapping("/onchain/wallet/{userId}/balance")
    @Operation(summary = "사용자 온체인 잔액 조회", description = "userId로 DB 잔액과 온체인 SSF 잔액을 비교 조회")
    public ResponseEntity<Map<String, Object>> getUserOnChainBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(mintingQueryService.getUserOnChainBalance(userId));
    }

    @GetMapping("/onchain/wallet/{userId}/tickets")
    @Operation(summary = "사용자 보유 TicketNFT 조회", description = "userId의 지갑이 온체인에서 소유한 TicketNFT 목록 조회")
    public ResponseEntity<Map<String, Object>> getUserOwnedTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(mintingQueryService.getUserOwnedTickets(userId));
    }

    @GetMapping("/onchain/settlement/{onChainSessionId}")
    @Operation(summary = "Settlement 예치 현황 조회", description = "온체인 회차 ID로 Settlement 컨트랙트에 예치된 SSF 금액 조회")
    public ResponseEntity<Map<String, Object>> getSettlementDeposit(@PathVariable Long onChainSessionId) {
        return ResponseEntity.ok(mintingQueryService.getSettlementDeposit(onChainSessionId));
    }

    @GetMapping("/test/available-seats/{showId}")
    @Operation(summary = "AVAILABLE 좌석 랜덤 조회 (테스트용)", description = "구매 테스트용 — MINTED 공연의 AVAILABLE 좌석을 랜덤으로 반환")
    public ResponseEntity<Map<String, Object>> getAvailableSeats(@PathVariable Long showId,
        @RequestParam(defaultValue = "3") int count) {
        return ResponseEntity.ok(mintingQueryService.getAvailableSeats(showId, count));
    }
}
