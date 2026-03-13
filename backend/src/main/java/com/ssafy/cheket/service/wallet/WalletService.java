package com.ssafy.cheket.service.wallet;

import com.ssafy.cheket.dto.wallet.response.WalletBalanceResponse;

/**
 * 지갑 서비스 — 회원가입 시 플랫폼 → 신규 유저/호스트 지갑으로 초기 SSF 전송
 */
public interface WalletService {

    /**
     * 회원가입 시 플랫폼 지갑 → 신규 유저/호스트 지갑으로 초기 SSF 전송
     *
     * @param toAddress
     *            수신자 지갑 주소 (블록체인 주소)
     * @param userId
     *            일반 유저의 users.id (트랜잭션 DB 기록에 사용) 호스트 가입 시 null 전달 → 트랜잭션 DB 기록 안 함
     *
     *            [왜 호스트는 Transaction을 안 만드는가?] - Transaction 테이블의 buyer_id는
     *            users(id) FK → 호스트 ID를 넣으면 FK 위반 - 호스트는 공연 주최자로, 티켓을 사고파는 주체가 아님 -
     *            호스트에게 보내는 초기 SSF는 단순 운영 지원금 → 거래 내역에 기록할 필요 없음 - 블록체인에는 당연히 기록됨
     *            (온체인 내역으로 추적 가능) - 이벤트 리스너가 지갑 잔액(ctkBalance)은 자동 업데이트해줌
     */
    void transferInitialFunds(String toAddress, Long userId);

    // 기본 조회 - DB에서 읽기 (빠름)
    WalletBalanceResponse getBalance(Long userId);

    // 새로고침 - 온체인 balanceOf() 호출 -> DB 동기화
    WalletBalanceResponse refreshBalance(Long userId);
}
