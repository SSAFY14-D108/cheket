package com.ssafy.cheket.service.blockchain;

import java.util.Map;

/**
 * ShowMintingService — 공연 민팅 전담 인터페이스
 *
 * [기획서 7.1 — 2단계: 예매 오픈 D-1] 예매 시작 전날에 스케줄러가 자동 실행하여: ① EventNFT 발행 (공연 정보 +
 * StakeholderNFT 참조 + 예매 규칙 온체인 확정) ② EventNFT.addSession() (회차별 등록) ③
 * EventNFT.setRefundPolicy() (환불 정책 온체인 확정) ④ TicketNFT.batchMintTickets()
 * (100개씩 배치 발행, 플랫폼 지갑 소유)
 *
 * [상태 전이] DRAFT → MINTING → MINTED DRAFT: 공연 정보 입력 완료, 아직 온체인 미등록 MINTING:
 * EventNFT + TicketNFT 발행 중 (중간 상태) MINTED: 발행 완료, 예매 가능 상태
 *
 * [호출자] - BlockchainScheduler: 매일 새벽 3시 자동 민팅 (mintPendingDraftShows) -
 * MintingController: 수동 단건/일괄 민팅 API
 */
public interface ShowMintingService {

    /**
     * 공연 단건 민팅
     *
     * DRAFT 상태의 공연 1건에 대해 EventNFT + TicketNFT 전체 발행
     *
     * [실행 흐름] ① DRAFT → MINTING 상태 전이 ② EventNFT.createEvent() — 공연 정보 온체인 등록 ③
     * EventNFT.addSession() × N — 회차별 등록 ④ EventNFT.setRefundPolicy() — 환불 정책 온체인
     * 등록 ⑤ TicketNFT.batchMintTickets() × M — 100개씩 배치 발행 ⑥ MINTING → MINTED 상태 전이
     *
     * 실패 시 DRAFT로 복원 (재시도 가능) 온체인 TX는 롤백 불가 (블록체인 특성)
     *
     * @param showId
     *            공연 ID
     */
    void mintShowNfts(Long showId);

    /**
     * 예매 시작일이 내일 이하인 DRAFT 공연 일괄 민팅 (수동 API용)
     *
     * [범위] reservationStartDate ≤ 내일 23:59:59 → 과거 예매 시작 + 오늘 + 내일 전부 포함 → 스케줄러가 놓친
     * 공연도 수동으로 복구 가능
     *
     * [처리 방식] 순차 처리, 실패해도 다음 공연 계속 진행
     *
     * @return 민팅 결과 (total, success, failed 카운트)
     */
    Map<String, Object> mintAllDraftShows();

    /**
     * 예매 시작일이 내일 이하인 DRAFT 공연 자동 민팅 (스케줄러용)
     *
     * [대상] reservationStartDate ≤ 내일 23:59:59 인 DRAFT 공연 전부 → 내일 예매 시작: 정상 D-1 민팅 →
     * 과거 예매 시작인데 DRAFT: 스케줄러 다운/실패로 놓친 공연 자동 복구
     *
     * [처리 방식] 순차 처리 (플랫폼 지갑 1개 → 병렬 시 Nonce 충돌) 실패 시 최대 5회 즉시 재시도
     *
     * @return 민팅 시도한 공연 수
     */
    int mintPendingDraftShows();
}
