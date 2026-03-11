package com.ssafy.cheket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

// Web3j 설정 — SSAFY 블록체인 네트워크 연결용
@Configuration
public class Web3jConfig {
    // SSAFY 네트워크 RPC URL
    @Value("${blockchain.rpc-url")
    private String rpcUrl;

    // Web3j 빈 등록 — 블록체인 트랜잭션 전송 시 사용
    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(rpcUrl));
    }
}
