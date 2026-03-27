package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.entity.settlement.Settlement;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.settlement.SettlementRepository;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SettlementServiceImpl — 1차 판매금 자동 정산 구현체
 *
 * [핵심 원리] "서버는 트리거만 할 뿐, 분배 비율은 StakeholderNFT에 고정"
 *
 * Settlement.finalizeSession(onChainSessionId, eventNftId) 호출 → 컨트랙트 내부에서: ①
 * sessionDeposits 총액 확인 (예치된 SSF) ② EventNFT → StakeholderNFT ID 목록 조회 ③ 각
 * StakeholderNFT의 shareBps로 SSF 분배 ④ EventNFT.finalizeSession() → isFinalized =
 * true (이중 정산 방지)
 *
 * [정산 흐름] 1차 판매 시: SSF → Settlement 컨트랙트 잠금 공연 종료 후: finalizeSession() →
 * StakeholderNFT 비율대로 분배 → 주최자 52%, 아티스트 20%, 플랫폼 8% 등
 *
 * [DB 이력] 정산 성공 시 settlements 테이블에 이해관계자별 1건씩 기록 → show_id, session_id,
 * stakeholder_id, tx_hash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final BlockchainService blockchainService;
    private final SessionRepository sessionRepository;
    private final ShowRepository showRepository;
    private final StakeholderRepository stakeholderRepository;
    private final SettlementRepository settlementRepository;

    @Override
    public List<Long> finalizeCompletedSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> settledSessionIds = new ArrayList<>();

        List<Session> sessions = sessionRepository.findAllCompletedSessions(now);

        for (Session session : sessions) {
            try {
                Show show = showRepository.findById(session.getShowId()).orElse(null);
                if (show == null) {
                    log.warn("[정산] 회차 {} 공연 없음 → 스킵 (showId={})", session.getId(), session.getShowId());
                    continue;
                }

                // 온체인에서 이미 정산됐는지 확인 (이중 정산 방지)
                boolean isFinalized = blockchainService.getSettlement()
                    .isSessionFinalized(BigInteger.valueOf(session.getOnChainSessionId())).send();
                if (isFinalized) {
                    continue;
                }

                // 예치금 확인 (0이면 판매 안 된 것 → 스킵)
                BigInteger deposits = blockchainService.getSettlement()
                    .getSessionDeposits(BigInteger.valueOf(session.getOnChainSessionId())).send();
                if (deposits.compareTo(BigInteger.ZERO) == 0) {
                    log.info("[정산] 회차 {} 예치금 없음 → 스킵", session.getId());
                    continue;
                }

                // 정산 실행 (실패 시 재시도)
                finalizeWithRetry(session, show);
                settledSessionIds.add(session.getId());

            } catch (Exception e) {
                log.error("[정산] 회차 {} 최종 실패 — {}: {}", session.getId(), e.getClass().getSimpleName(), e.getMessage());
            }
        }

        return settledSessionIds;
    }

    @Override
    public List<Long> finalizeByShowId(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다: " + showId));

        if (show.getEventNftId() == null) {
            throw new BlockchainException("민팅되지 않은 공연입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Session> sessions = sessionRepository.findCompletedSessionsByShowId(showId, now);
        List<Long> settledSessionIds = new ArrayList<>();

        for (Session session : sessions) {
            try {
                boolean isFinalized = blockchainService.getSettlement()
                    .isSessionFinalized(BigInteger.valueOf(session.getOnChainSessionId())).send();
                if (isFinalized) {
                    log.info("[정산] 회차 {} 이미 정산됨 → 스킵", session.getId());
                    continue;
                }

                BigInteger deposits = blockchainService.getSettlement()
                    .getSessionDeposits(BigInteger.valueOf(session.getOnChainSessionId())).send();
                if (deposits.compareTo(BigInteger.ZERO) == 0) {
                    log.info("[정산] 회차 {} 예치금 없음 → 스킵", session.getId());
                    continue;
                }

                // 정산 실행 (실패 시 재시도)
                finalizeWithRetry(session, show);
                settledSessionIds.add(session.getId());

            } catch (Exception e) {
                log.error("[정산] 회차 {} 최종 실패 — {}: {}", session.getId(), e.getClass().getSimpleName(), e.getMessage());
            }
        }

        return settledSessionIds;
    }

    @Override
    public void finalizeBySessionId(Long showId, Long sessionId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다: " + showId));

        if (show.getEventNftId() == null) {
            throw new BlockchainException("민팅되지 않은 공연입니다.");
        }

        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다: " + sessionId));

        if (!session.getShowId().equals(showId)) {
            throw new BlockchainException("회차 " + sessionId + "는 공연 " + showId + "에 속하지 않습니다.");
        }

        if (session.getOnChainSessionId() == null) {
            throw new BlockchainException("온체인에 등록되지 않은 회차입니다.");
        }

        // 종료 여부 확인 (sessionStartTime + playtime < now)
        LocalDateTime sessionEndTime = session.getSessionStartTime().plusMinutes(show.getPlaytime());
        if (sessionEndTime.isAfter(LocalDateTime.now())) {
            throw new BlockchainException("아직 종료되지 않은 회차입니다.");
        }

        try {
            boolean isFinalized = blockchainService.getSettlement()
                .isSessionFinalized(BigInteger.valueOf(session.getOnChainSessionId())).send();
            if (isFinalized) {
                throw new BlockchainException("이미 정산된 회차입니다.");
            }

            BigInteger deposits = blockchainService.getSettlement()
                .getSessionDeposits(BigInteger.valueOf(session.getOnChainSessionId())).send();
            if (deposits.compareTo(BigInteger.ZERO) == 0) {
                throw new BlockchainException("예치금이 없는 회차입니다.");
            }
        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            throw new BlockchainException("온체인 조회 실패: " + e.getMessage());
        }

        finalizeSession(session, show);
    }

    private static final int MAX_RETRY = 5;

    /**
     * 정산 실행 + 재시도 (최대 5회)
     *
     * 블록체인 통신 실패 시 재시도, 비즈니스 예외(BlockchainException)는 즉시 실패
     */
    private void finalizeWithRetry(Session session, Show show) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                finalizeSession(session, show);
                return;
            } catch (BlockchainException e) {
                // 비즈니스 예외 (stakeholder 없음, 분배율 오류 등)는 재시도 불필요
                throw e;
            } catch (Exception e) {
                log.warn("[정산] 회차 {} 재시도 {}/{} — {}", session.getId(), attempt, MAX_RETRY, e.getMessage());
                if (attempt == MAX_RETRY) {
                    throw new BlockchainException(
                        "정산 " + MAX_RETRY + "회 재시도 실패 — sessionId=" + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * 단일 회차 정산 실행
     *
     * [온체인 처리] Settlement.finalizeSession(onChainSessionId, eventNftId) 호출
     *
     * [컨트랙트 내부 동작] ① sessionDeposits[sessionId] 총액 확인 ②
     * EventNFT.getStakeholderTokenIds(eventId) → [0, 1, 2] ③ 각 StakeholderNFT의
     * shareBps로 SSF 분배 ④ EventNFT.finalizeSession() → isFinalized = true
     *
     * [DB 이력] 정산 성공 후 settlements 테이블에 이해관계자별 1건씩 저장
     */
    private void finalizeSession(Session session, Show show) {
        // 이해관계자 존재 여부 검증
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(show.getId());
        if (stakeholders.isEmpty()) {
            throw new BlockchainException("이해관계자가 등록되지 않은 공연입니다: showId=" + show.getId());
        }

        // 분배율 합계 검증 (10000 bps = 100%)
        int totalBps = stakeholders.stream().mapToInt(Stakeholder::getShareBps).sum();
        if (totalBps != 10000) {
            throw new BlockchainException(
                "이해관계자 분배율 합계가 100%가 아닙니다: " + totalBps + " bps (showId=" + show.getId() + ")");
        }

        try {
            BigInteger onChainSessionId = BigInteger.valueOf(session.getOnChainSessionId());
            BigInteger eventNftId = BigInteger.valueOf(show.getEventNftId());

            BigInteger deposits = blockchainService.getSettlement().getSessionDeposits(onChainSessionId).send();

            log.info("[정산] 시작 — showId={}, sessionId={}, " + "onChainSessionId={}, eventNftId={}, " + "예치금={} SSF",
                show.getId(), session.getId(), onChainSessionId, eventNftId, deposits);

            TransactionReceipt receipt = blockchainService.executePlatformTx(
                () -> blockchainService.getSettlement().finalizeSession(onChainSessionId, eventNftId).send());

            String txHash = receipt.getTransactionHash();
            log.info("[정산] 완료 — sessionId={}, txHash={}, " + "총 {} SSF 분배", session.getId(), txHash, deposits);

            // 정산 이력 저장 — 이해관계자별 1건씩
            saveSettlementHistory(show, session, txHash, stakeholders);

        } catch (Exception e) {
            throw new BlockchainException("정산 실패 — sessionId=" + session.getId() + ": " + e.getMessage());
        }
    }

    /**
     * 정산 이력을 settlements 테이블에 저장
     *
     * 해당 공연의 모든 이해관계자(Stakeholder)마다 1건씩 기록 → 같은 txHash지만, 누구에게 분배됐는지 개별 추적 가능
     */
    private void saveSettlementHistory(Show show, Session session, String txHash, List<Stakeholder> stakeholders) {

        for (Stakeholder stakeholder : stakeholders) {
            Settlement settlement = Settlement.builder().showId(show.getId()).sessionId(session.getId())
                .stakeholderId(stakeholder.getId()).txHash(txHash).build();

            settlementRepository.save(settlement);

            log.info("[정산 이력] 저장 — sessionId={}, " + "stakeholderId={} ({}), txHash={}", session.getId(),
                stakeholder.getId(), stakeholder.getRole(), txHash);
        }
    }

    // ========== 온체인 조회 ==========

    @Override
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
}
