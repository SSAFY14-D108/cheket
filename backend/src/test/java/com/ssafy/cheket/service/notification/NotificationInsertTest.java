package com.ssafy.cheket.service.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
@Rollback(false)
class NotificationInsertTest {

    @Autowired
    private NotificationService notificationService;

    // 알림 내역 조회 테스트를 위한 알림 넣는 테스트. 실행하면 롤백 안되고 실제로 들어감
    @Test
    void 알림_데이터_직접_넣기() {
        notificationService.sendRequestCreate(1L, 2L);
        notificationService.sendRequestUpdate(1L, 3L);
        notificationService.sendResale(1L, "AESPA 2026 콘서트");
        notificationService.sendSettlement(1L, LocalDateTime.of(2026, 3, 21, 18, 30), "AESPA 2026 콘서트",
            new BigDecimal("100"));
        notificationService.sendShowStart(1L, LocalDateTime.of(2026, 3, 23, 18, 30), "AESPA 2026 콘서트");
    }
}
