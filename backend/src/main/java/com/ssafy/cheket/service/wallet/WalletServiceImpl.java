package com.ssafy.cheket.service.wallet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final Web3j web3j;

    // 플랫폼 지갑 프라이빗 키
    @Value("${blockchain.platform-private-key}")
    private String platformPrivateKey;

    // 초기 전송 금액 (SSF)
    @Value("${blockchain.initial-transfer-amount}")
    private long initialTransferAmount;

    // 비동기로 플랫폼 지갑 → 신규 유저 지갑으로 SSF 전송
    @Async
    @Override
    public void transferInitialFunds(String toAddress) {
        try {
            // 플랫폼 지갑 인증 정보 로드
            Credentials platformCredentials = Credentials.create(platformPrivateKey);

            // SSF 전송 (네이티브 토큰이므로 ETHER 단위 사용)
            Transfer.sendFunds(
                web3j,
                platformCredentials,
                toAddress,
                BigDecimal.valueOf(initialTransferAmount),
                Convert.Unit.ETHER
            ).send();

            log.info("초기 SSF 전송 성공: {} → {} SSF", toAddress, initialTransferAmount);
        } catch (Exception e) {
            log.error("초기 SSF 전송 실패: {}", toAddress, e);
        }
    }

}
