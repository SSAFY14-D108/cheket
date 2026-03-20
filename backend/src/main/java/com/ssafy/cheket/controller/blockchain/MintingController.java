package com.ssafy.cheket.controller.blockchain;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.service.blockchain.ShowMintingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MintingController — 블록체인 민팅 테스트용 API
 *
 * 스케줄러(새벽 3시)를 기다리지 않고 수동으로 민팅을 트리거할 수 있다.
 * 개발/테스트 환경에서 사용. 운영 시에는 비활성화 권장.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/minting")
@Tag(name = "Minting (테스트)", description = "블록체인 민팅 테스트 API")
public class MintingController {

    private final ShowMintingService showMintingService;
    private final ShowRepository showRepository;

    /**
     * DRAFT 상태인 공연 단건 민팅 (EventNFT + TicketNFT)
     */
    @PostMapping("/shows/{showId}")
    @Operation(summary = "공연 단건 민팅", description = "DRAFT 상태의 공연을 수동으로 민팅 (EventNFT + TicketNFT 발행)")
    public ResponseEntity<Map<String, Object>> mintShow(@PathVariable Long showId) {
        log.info("[민팅 API] 공연 단건 민팅 요청 — showId={}", showId);
        showMintingService.mintShowNfts(showId);
        return ResponseEntity.ok(Map.of(
            "message", "민팅 완료",
            "showId", showId
        ));
    }

    /**
     * DRAFT 상태인 공연 전체 민팅
     */
    @PostMapping("/shows/all")
    @Operation(summary = "DRAFT 공연 전체 민팅", description = "DRAFT 상태의 모든 공연을 순차적으로 민팅")
    public ResponseEntity<Map<String, Object>> mintAllDraftShows() {
        List<Show> draftShows = showRepository
            .findByStatusAndReservationStartDateBetween(
                ShowStatus.DRAFT,
                java.time.LocalDateTime.MIN,
                java.time.LocalDateTime.MAX
            );

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

        return ResponseEntity.ok(Map.of(
            "message", "전체 민팅 완료",
            "total", draftShows.size(),
            "success", success,
            "failed", failed
        ));
    }
}
