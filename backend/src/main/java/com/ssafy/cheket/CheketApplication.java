package com.ssafy.cheket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync // @Async 활성화 → WalletServiceImpl.transferInitialFunds()가 별도 스레드에서 실행
@EnableScheduling // @Scheduled 활성화 → SsfTransferEventListener.pollTransferEvents()가 5초마다 실행
public class CheketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheketApplication.class, args);
    }
}
