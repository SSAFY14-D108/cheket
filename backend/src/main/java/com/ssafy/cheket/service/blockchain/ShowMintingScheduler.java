package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.repository.show.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * ShowMintingScheduler — 예매 오픈 D-1 자동 발행 스케줄러
 *
 * [기획서 7.1 — 2단계] "백엔드 스케줄러 (예매 오픈 전날 자동 실행) → EventNFT 발행 + TicketNFT 배치 발행 →
 * ShowStatus: DRAFT → MINTING → MINTED"
 *
 * [실행 주기] 매일 새벽 3시에 실행 (cron = "0 0 3 * * *") → "내일 예매 시작인 공연" 검색 → 각 공연에 대해
 * EventNFT + TicketNFT 발행
 *
 * [왜 새벽 3시?] 사용자 트래픽이 가장 적은 시간 블록체인 TX 처리가 다른 작업과 충돌하지 않도록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShowMintingScheduler {

    private final ShowMintingService showMintingService;
    private final ShowRepository showRepository;

    // 실패 시 최대 재시도 횟수
    // 5회면 일시적 네트워크 에러는 대부분 복구됨
    // 5회 다 실패하면 코드/컨트랙트 문제일 가능성 → 관리자 확인 필요
    private static final int MAX_RETRY = 5;

    /**
     * 매일 새벽 3시에 실행 "내일 예매 시작인 DRAFT 상태의 공연"을 찾아서 민팅
     *
     * cron = "초 분 시 일 월 요일" "0 0 3 * * *" = 매일 03:00:00
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleMinting() {
        // "내일" 범위 계산: 내일 00:00:00 ~ 내일 23:59:59
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime from = tomorrow.atStartOfDay();
        LocalDateTime to = tomorrow.atTime(LocalTime.MAX);

        // 예매 시작일이 내일이고, 아직 DRAFT인 공연 조회
        List<Show> showsToMint = showRepository.findByStatusAndReservationStartDateBetween(ShowStatus.DRAFT, from, to);

        if (showsToMint.isEmpty()) {
            log.info("[민팅 스케줄러] 내일 예매 시작인 공연 없음");
            return;
        }

        log.info("[민팅 스케줄러] 민팅 대상 공연 {}건 발견", showsToMint.size());

        // 각 공연에 대해 순차적으로 민팅 실행
        // (병렬 처리하면 nonce 충돌 위험)
        for (Show show : showsToMint) {
            mintWithRetry(show);
        }
    }

    /**
     * 공연 1건 민팅 + 실패 시 즉시 재시도 (최대 MAX_RETRY회)
     *
     * [재시도 안전성] 실패 시 @Transactional이 DB를 롤백하고 DRAFT로 복원 → 재시도 시 깨끗한 상태에서 새로 발행 → 1차
     * 시도의 온체인 NFT는 고아로 남지만 DB 매핑이 없어 무해 → SSAFY 네트워크 gasPrice=0이라 비용 부담 없음
     */
    private void mintWithRetry(Show show) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                log.info("[민팅 스케줄러] 공연 '{}' (ID:{}) 민팅 시도 {}/{}", show.getTitle(), show.getId(), attempt, MAX_RETRY);
                showMintingService.mintShowNfts(show.getId());
                log.info("[민팅 스케줄러] 공연 '{}' (ID:{}) 민팅 완료", show.getTitle(), show.getId());
                return; // 성공하면 즉시 종료
            } catch (Exception e) {
                if (attempt == MAX_RETRY) {
                    // 5회 모두 실패 → 관리자 확인 필요
                    log.error("[민팅 스케줄러] 공연 '{}' (ID:{}) 민팅 {}회 모두 실패 — 관리자 확인 필요", show.getTitle(), show.getId(),
                        MAX_RETRY, e);
                    // TODO: 관리자 알림 (Push/Slack 등)
                } else {
                    log.warn("[민팅 스케줄러] 공연 '{}' (ID:{}) 민팅 {}회차 실패, 즉시 재시도", show.getTitle(), show.getId(), attempt, e);
                }
            }
        }
    }
}
