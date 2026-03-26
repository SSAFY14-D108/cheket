package com.ssafy.cheket.service.blockchain;

import java.util.Map;

/**
 * MintingService — 온체인 조회 + 유틸 인터페이스
 *
 * [역할] ① 온체인 조회: showId 기반 통합 상태 조회 (StakeholderNFT + EventNFT + 회차 +
 * TicketNFT) ② 유틸: 좌석↔온체인 매핑, 사용자 잔액 비교, 보유 TicketNFT 조회 등
 *
 * [사용처] MintingController에서 호출 — 테스트/디버깅/관리자용 API
 *
 * [민팅은 ShowMintingService가 담당] 공연 민팅 관련 (단건/일괄/스케줄러) → ShowMintingService 온체인
 * 조회/유틸 → MintingService (여기) → 민팅 로직과 조회 로직 분리로 단일 책임 원칙 준수
 */
public interface MintingService {

    // ========== 온체인 통합 조회 ==========

    /**
     * showId 기반 온체인 통합 상태 조회
     *
     * StakeholderNFT + EventNFT + 회차(Session) + TicketNFT 샘플을 한 번에 조회하여 DB 상태와 온체인
     * 상태를 비교할 수 있는 데이터 반환
     *
     * @param showId
     *            공연 ID
     * @return DB 상태 + 온체인 상태 통합 결과
     */
    Map<String, Object> getShowOnChainStatus(Long showId);

    // ========== 유틸 ==========

    /**
     * 좌석 → 온체인 티켓 매핑 조회
     *
     * sessionSeatId로 DB 좌석 정보 + 온체인 TicketNFT 정보를 한번에 조회
     *
     * @param sessionSeatId
     *            좌석 ID
     * @return DB 좌석 상태 + 온체인 owner/section/row/seat/grade/price
     */
    Map<String, Object> getSeatOnChainMapping(Long sessionSeatId);

    /**
     * 공연 전체 좌석 + 온체인 매핑 조회
     *
     * showId로 전체 좌석의 DB 상태 + 온체인 owner를 조회 (판매 현황 파악)
     *
     * @param showId
     *            공연 ID
     * @return 회차별 전체 좌석 목록 + available/pendingTx/sold 카운트
     */
    Map<String, Object> getShowSeatsMapping(Long showId);

    /**
     * 사용자 온체인 잔액 조회
     *
     * userId로 DB 잔액(ctkBalance)과 온체인 SSF 잔액(balanceOf)을 비교 → isMatch로 동기화 상태 확인 가능
     *
     * @param userId
     *            사용자 ID
     * @return DB 잔액 + 온체인 잔액 + 일치 여부
     */
    Map<String, Object> getUserOnChainBalance(Long userId);

    /**
     * 사용자 보유 TicketNFT 조회
     *
     * userId의 지갑이 온체인에서 실제로 소유한 TicketNFT 목록 조회 전체 totalSupply를 순회하며 ownerOf 매칭
     *
     * @param userId
     *            사용자 ID
     * @return 보유 TicketNFT 목록 (tokenId, section, row, seat 등)
     */
    Map<String, Object> getUserOwnedTickets(Long userId);

    /**
     * AVAILABLE 좌석 랜덤 조회 (테스트용)
     *
     * 구매 테스트용 — MINTED 공연의 AVAILABLE 좌석을 랜덤으로 반환
     *
     * @param showId
     *            공연 ID
     * @param count
     *            반환할 좌석 수
     * @return 회차별 랜덤 AVAILABLE 좌석 목록
     */
    Map<String, Object> getAvailableSeats(Long showId, int count);

    // ========== 온체인/DB 동기화 ==========

    /**
     * 공연의 모든 좌석에 대해 온체인 NFT 소유자와 DB 상태를 비교하고 불일치 시 DB를 교정
     */
    Map<String, Object> syncShowWithOnChain(Long showId);
}
