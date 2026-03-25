package com.ssafy.cheket.service.blockchain;

import java.util.List;
import java.util.Map;

/**
 * SettlementService — 1차 판매금 자동 정산 인터페이스
 *
 * [핵심 원리 — 기획서 5.5] "서버는 트리거만 할 뿐, 분배 비율은 StakeholderNFT에 고정" → 기존 플랫폼: 서버가 직접
 * 계산해서 분배 → 비율 조작 가능 → CHEKET: 컨트랙트가 온체인 데이터로 직접 분배 → 조작 불가
 *
 * [정산 흐름] 1차 판매 시: SSF → Settlement 컨트랙트에 잠금 (예치) 공연 종료 후: finalizeSession() 호출
 * → 컨트랙트가 StakeholderNFT의 shareBps 비율대로 SSF 자동 분배 → 주최자 52%, 아티스트 20%, 플랫폼 8% 등
 * → EventNFT.isFinalized = true (이중 정산 방지)
 *
 * [DB 이력] 정산 성공 시 settlements 테이블에 이해관계자별 1건씩 기록 → show_id, session_id,
 * stakeholder_id, tx_hash → 일반 유저/사업자 모두 자신의 정산 내역 조회 가능
 *
 * [사용처] - BlockchainScheduler: 매일 아침 9시 자동 정산 (finalizeCompletedSessions) -
 * MintingController: 수동 정산 API (finalizeByShowId)
 */
public interface SettlementService {

    /**
     * 종료된 모든 회차 자동 정산 (스케줄러용)
     *
     * [정산 조건 — 4가지 모두 충족해야 정산 실행] ① 공연 종료됨 (sessionStartTime + playtime < 현재) ② 민팅
     * 완료됨 (onChainSessionId IS NOT NULL) ③ 아직 정산 안 됨 (온체인 isFinalized == false) ④
     * 예치금 있음 (sessionDeposits > 0)
     *
     * [에러 처리] 한 회차 실패해도 다른 회차는 계속 처리 (부분 실패 허용)
     *
     * @return 정산 완료된 session ID 목록
     */
    List<Long> finalizeCompletedSessions();

    /**
     * 특정 공연의 모든 회차 즉시 정산 (수동 API용)
     *
     * 스케줄러 기다리지 않고 즉시 정산 트리거 테스트나 긴급 정산 시 사용
     *
     * [사전 검증] - 공연 존재 여부 - EventNFT 발행(민팅) 완료 여부 - 온체인 이중 정산 방지 (isFinalized 체크)
     *
     * @param showId
     *            공연 ID
     * @return 정산 완료된 session ID 목록
     */
    List<Long> finalizeByShowId(Long showId);

    /**
     * 특정 회차 1건 즉시 정산 (수동 API용)
     *
     * 공연 ID + 회차 ID를 지정해서 단일 회차만 정산 특정 회차만 긴급 정산하거나 테스트할 때 사용
     *
     * [사전 검증] - 공연/회차 존재 여부 - EventNFT 발행(민팅) 완료 여부 - onChainSessionId 존재 여부 - 온체인
     * 이중 정산 방지 (isFinalized 체크) - 예치금 존재 여부
     *
     * @param showId
     *            공연 ID
     * @param sessionId
     *            회차 ID
     */
    void finalizeBySessionId(Long showId, Long sessionId);

    /**
     * Settlement 예치 현황 조회
     *
     * 온체인 회차 ID로 Settlement 컨트랙트에 예치된 SSF 금액 조회 → 1차 판매 시 PurchaseRouter가 잠근 SSF 총액
     * 확인
     *
     * @param onChainSessionId
     *            온체인 회차 ID
     * @return 예치된 SSF 금액
     */
    Map<String, Object> getSettlementDeposit(Long onChainSessionId);
}
