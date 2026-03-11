package com.ssafy.cheket.service.wallet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

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

    // SSF ERC-20 컨트랙트 주소
    @Value("${blockchain.ssf-contract-address}")
    private String ssfContractAddress;

    // 체인 ID
    @Value("${blockchain.chain-id}")
    private long chainId;

    // 비동기로 플랫폼 지갑 → 신규 유저 지갑으로 SSF(ERC-20) 전송
    @Async
    @Override
    public void transferInitialFunds(String toAddress) {
        try {
            Credentials platformCredentials = Credentials.create(platformPrivateKey);

            // ERC-20 토큰 전송량 (18 decimals)
            BigInteger tokenAmount = BigInteger.valueOf(initialTransferAmount).multiply(BigInteger.TEN.pow(18));

            // ERC-20 transfer(address, uint256) 함수 인코딩
            Function function = new Function("transfer",
                Arrays.asList(new Address(toAddress), new Uint256(tokenAmount)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));

            String encodedFunction = FunctionEncoder.encode(function);

            RawTransactionManager txManager = new RawTransactionManager(web3j, platformCredentials, chainId);

            EthSendTransaction tx = txManager.sendTransaction(DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT, ssfContractAddress, encodedFunction, BigInteger.ZERO);

            log.info("초기 SSF 전송 성공: {} → {} SSF (tx: {})", toAddress, initialTransferAmount, tx.getTransactionHash());
        } catch (Exception e) {
            log.error("초기 SSF 전송 실패: {}", toAddress, e);
        }
    }

}
