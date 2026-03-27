package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BlockchainScheduler — 블록체인 관련 스케줄러 통합
 *
 * [역할] ① 매일 새벽 3시: 예매 오픈 D-1 공연 자동 민팅 (EventNFT + TicketNFT) ② 매일 아침 9시: 종료된 공연
 * 회차 자동 정산 (Settlement.finalizeSession)
 *
 * [왜 순차 처리?] 플랫폼 지갑 1개로 TX를 보내니까 병렬로 하면 Nonce 충돌 → 3시 민팅 끝나고 → 9시 정산 시작
 *
 * [스케줄러의 역할은 "트리거만"] 실제 비즈니스 로직은 각 Service에 위임 → ShowMintingService: 민팅 대상 조회 +
 * 재시도 + 발행 → SettlementService: 정산 대상 조회 + 온체인 정산 + 이력 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockchainScheduler {

    private final ShowMintingService showMintingService;
    private final SettlementService settlementService;
    private final SessionSeatRepository sessionSeatRepository;
    private final TicketRepository ticketRepository;

    // ========== ① 민팅 스케줄러 (매일 새벽 3시) ==========

    /**
     * 예매 오픈 D-1 공연 자동 민팅
     *
     * 내일 예매 시작인 DRAFT 공연을 찾아서 EventNFT + TicketNFT를 온체인에 배치 발행 → DRAFT → MINTING →
     * MINTED
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduleShowAndTicketMinting() {
        log.info("[민팅 스케줄러] 실행 시작");

        try {
            int count = showMintingService.mintPendingDraftShows();
            log.info("[민팅 스케줄러] 실행 완료 — 대상 {}건", count);
        } catch (Exception e) {
            log.error("[민팅 스케줄러] 실행 중 에러 — {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    // ========== ② 정산 스케줄러 (매일 아침 9시) ==========

    /**
     * 종료된 공연 회차 자동 정산
     *
     * 공연 종료 후 Settlement.finalizeSession() 호출 → 컨트랙트가 StakeholderNFT shareBps 비율대로
     * SSF 자동 분배 → 서버는 트리거만, 분배 비율은 온체인에 고정
     *
     * [정산 조건] ① 공연 종료됨 (sessionStartTime + playtime < 현재) ② 민팅 완료됨
     * (onChainSessionId IS NOT NULL) ③ 아직 정산 안 됨 (온체인 isFinalized == false) ④ 예치금
     * 있음 (sessionDeposits > 0)
     */
    // ========== ③ PENDING_TX 좀비 좌석 복원 (10분마다) ==========

    /**
     * PENDING_TX 고착 좌석 자동 복원
     *
     * 구매/환불 중 블록체인 실패 + DB 복원 실패가 겹치면 좌석이 PENDING_TX로 영구 고착될 수 있음.
     * 15분 이상 PENDING_TX 상태인 좌석을 찾아서 복원:
     * - Ticket 레코드 존재 → 환불 중 고착 → SOLD 복원
     * - Ticket 레코드 없음 → 구매 중 고착 → AVAILABLE 복원
     */
    @Scheduled(fixedDelay = 600000)
    public void recoverStuckPendingTxSeats() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<SessionSeat> stuckSeats = sessionSeatRepository.findStuckPendingTxSeats(threshold);

        if (stuckSeats.isEmpty()) return;

        log.warn("[좌석 복원 스케줄러] PENDING_TX 고착 좌석 {}개 발견", stuckSeats.size());

        for (SessionSeat seat : stuckSeats) {
            boolean hasTicket = ticketRepository.existsBySessionSeatId(seat.getId());
            SeatStatus restoreStatus = hasTicket ? SeatStatus.SOLD : SeatStatus.AVAILABLE;
            seat.setStatus(restoreStatus);
            log.warn("[좌석 복원] sessionSeatId={} → {} (Ticket 존재: {})", seat.getId(), restoreStatus, hasTicket);
        }

        sessionSeatRepository.saveAll(stuckSeats);
        log.warn("[좌석 복원 스케줄러] {}개 좌석 복원 완료", stuckSeats.size());
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleSettlement() {
        log.info("[정산 스케줄러] 실행 시작");

        try {
            List<Long> settledSessionIds = settlementService.finalizeCompletedSessions();

            if (settledSessionIds.isEmpty()) {
                log.info("[정산 스케줄러] 정산 대상 회차 없음");
            } else {
                log.info("[정산 스케줄러] 정산 완료 — {}건: {}", settledSessionIds.size(), settledSessionIds);
            }
        } catch (Exception e) {
            log.error("[정산 스케줄러] 실행 중 에러 — {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
