package com.ssafy.cheket.controller.blockchain;

import com.ssafy.cheket.service.blockchain.MintingService;
import com.ssafy.cheket.service.blockchain.SettlementService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * BlockchainController — 블록체인 관련 통합 API (테스트/관리자용)
 *
 * [역할] ① 민팅: 공연 단건/일괄 민팅 트리거 (ShowMintingService) ② 정산: 수동 정산 트리거 + 예치 현황 조회
 * (SettlementService) ③ 온체인 조회: 공연 통합 상태, 좌석 매핑, 잔액 비교 등 (MintingService)
 *
 * [왜 하나로 통합?] 모든 API가 테스트/관리자/디버깅용 → 프론트에서 직접 호출하지 않음 → Swagger 태그로 섹션 구분하면 충분
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blockchain")
@Tag(name = "Blockchain (테스트/관리자)", description = "블록체인 민팅 · 정산 · 온체인 조회 API")
public class BlockchainController {

    private final ShowMintingService showMintingService;
    private final MintingService mintingService;
    private final SettlementService settlementService;

    // ========== 민팅 ==========

    @PostMapping("/minting/shows/{showId}")
    @Operation(summary = "[민팅] 공연 단건 민팅", description = "DRAFT 상태의 공연을 수동으로 민팅" + " (EventNFT + TicketNFT 발행)")
    public ResponseEntity<Map<String, Object>> mintShow(@PathVariable Long showId) {
        log.info("[민팅 API] 공연 단건 민팅 요청 — showId={}", showId);
        Map<String, Object> result = showMintingService.mintShowNfts(showId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/minting/shows/all")
    @Operation(summary = "[민팅] DRAFT 공연 일괄 민팅 (내일 이하)", description = "예매 시작일이 내일 이하인 DRAFT 공연을 순차 민팅"
        + " — 스케줄러가 놓친 공연 복구")
    public ResponseEntity<Map<String, Object>> mintAllDraftShows() {
        return ResponseEntity.ok(showMintingService.mintAllDraftShows());
    }

    // ========== 정산 ==========

    @PostMapping("/settlement/finalize-all")
    @Operation(summary = "[정산] 전체 자동 정산", description = "종료된 모든 공연의 미정산 회차를 즉시 정산" + " — 스케줄러(매일 9시)와 동일한 로직을 수동 트리거")
    public ResponseEntity<Map<String, Object>> finalizeAll() {
        log.info("[정산 API] 전체 정산 수동 트리거");
        List<Long> settledSessionIds = settlementService.finalizeCompletedSessions();

        return ResponseEntity.ok(Map.of("settledSessions", settledSessionIds, "count", settledSessionIds.size()));
    }

    @PostMapping("/settlement/{showId}/finalize")
    @Operation(summary = "[정산] 공연별 정산", description = "특정 공연의 종료된 미정산 회차들을 즉시 정산")
    public ResponseEntity<Map<String, Object>> finalizeByShow(@PathVariable Long showId) {
        log.info("[정산 API] 공연별 정산 요청 — showId={}", showId);
        List<Long> settledSessionIds = settlementService.finalizeByShowId(showId);

        return ResponseEntity
            .ok(Map.of("showId", showId, "settledSessions", settledSessionIds, "count", settledSessionIds.size()));
    }

    @PostMapping("/settlement/{showId}/sessions/{sessionId}/finalize")
    @Operation(summary = "[정산] 회차별 정산", description = "특정 공연의 특정 회차 1건만 즉시 정산")
    public ResponseEntity<Map<String, Object>> finalizeBySession(@PathVariable Long showId,
        @PathVariable Long sessionId) {
        log.info("[정산 API] 회차별 정산 요청 — showId={}, sessionId={}", showId, sessionId);
        settlementService.finalizeBySessionId(showId, sessionId);

        return ResponseEntity.ok(Map.of("message", "정산 완료", "showId", showId, "sessionId", sessionId));
    }

    @GetMapping("/settlement/{onChainSessionId}/deposit")
    @Operation(summary = "[정산] 예치 현황 조회", description = "온체인 회차 ID로 Settlement 컨트랙트에" + " 예치된 SSF 금액 조회")
    public ResponseEntity<Map<String, Object>> getSettlementDeposit(@PathVariable Long onChainSessionId) {
        return ResponseEntity.ok(settlementService.getSettlementDeposit(onChainSessionId));
    }

    // ========== 온체인 조회 ==========

    @GetMapping("/onchain/show/{showId}")
    @Operation(summary = "[조회] 공연 온체인 통합 상태", description = "showId로 StakeholderNFT + EventNFT"
        + " + 회차 + TicketNFT 온체인 상태를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getShowOnChainStatus(@PathVariable Long showId) {
        return ResponseEntity.ok(mintingService.getShowOnChainStatus(showId));
    }

    @GetMapping("/onchain/show/{showId}/seats")
    @Operation(summary = "[조회] 공연 전체 좌석 + 온체인 매핑", description = "showId로 전체 좌석의 DB 상태" + " + 온체인 owner를 조회 (판매 현황 파악)")
    public ResponseEntity<Map<String, Object>> getShowSeatsMapping(@PathVariable Long showId) {
        return ResponseEntity.ok(mintingService.getShowSeatsMapping(showId));
    }

    @GetMapping("/onchain/seat/{sessionSeatId}")
    @Operation(summary = "[조회] 좌석 → 온체인 티켓 매핑", description = "sessionSeatId로 DB 좌석 정보" + " + 온체인 TicketNFT 정보를 한번에 조회")
    public ResponseEntity<Map<String, Object>> getSeatOnChainMapping(@PathVariable Long sessionSeatId) {
        return ResponseEntity.ok(mintingService.getSeatOnChainMapping(sessionSeatId));
    }

    // ========== 사용자 온체인 조회 ==========

    @GetMapping("/onchain/wallet/{userId}/balance")
    @Operation(summary = "[조회] 사용자 온체인 잔액", description = "userId로 DB 잔액과 온체인 SSF 잔액을 비교 조회")
    public ResponseEntity<Map<String, Object>> getUserOnChainBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(mintingService.getUserOnChainBalance(userId));
    }

    @GetMapping("/onchain/wallet/{userId}/tickets")
    @Operation(summary = "[조회] 사용자 보유 TicketNFT", description = "userId의 지갑이 온체인에서" + " 소유한 TicketNFT 목록 조회")
    public ResponseEntity<Map<String, Object>> getUserOwnedTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(mintingService.getUserOwnedTickets(userId));
    }

    // ========== 온체인/DB 동기화 ==========

    @PostMapping("/sync/show/{showId}")
    @Operation(summary = "[동기화] 공연 온체인/DB 동기화", description = "공연의 모든 좌석에 대해 온체인 NFT 소유자와 DB 상태를 비교하고 불일치 시 DB를 교정")
    public ResponseEntity<Map<String, Object>> syncShowWithOnChain(@PathVariable Long showId) {
        log.info("[동기화 API] 공연 동기화 요청 — showId={}", showId);
        return ResponseEntity.ok(mintingService.syncShowWithOnChain(showId));
    }

    // ========== 테스트 유틸 ==========

    @GetMapping("/test/available-seats/{showId}")
    @Operation(summary = "[테스트] 구매 가능한 좌석 랜덤 조회", description = "구매 테스트용 — MINTED 공연의" + " AVAILABLE 좌석을 랜덤으로 반환")
    public ResponseEntity<Map<String, Object>> getAvailableSeats(@PathVariable Long showId,
        @RequestParam(defaultValue = "3") int count) {
        return ResponseEntity.ok(mintingService.getAvailableSeats(showId, count));
    }
}
