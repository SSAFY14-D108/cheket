package com.ssafy.cheket.service.wallet;

public interface WalletService {

    // 회원가입 시 플랫폼 지갑 → 신규 유저 지갑으로 초기 SSF 전송
    void transferInitialFunds(String toAddress);

}
