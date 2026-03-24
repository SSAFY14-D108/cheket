package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;
import com.ssafy.cheket.entity.notification.Notification;
import com.ssafy.cheket.enums.NotificationType;
import com.ssafy.cheket.repository.notification.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void 공통_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        CreateNotificationRequest request = CreateNotificationRequest.builder().userId(1L)
            .type(NotificationType.SHOW_START).title("공연 시작 알림")
            .variables(Map.of("sessionDateTime", "2026/03/23 18:30", "showTitle", "락 페스티벌")).build();

        notificationService.createNotification(request);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("공연 시작 알림");
        assertThat(saved.getMessage()).isEqualTo("2026/03/23 18:30에 락 페스티벌 공연이 시작합니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void 공연_시작_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        notificationService.sendShowStart(1L, LocalDateTime.of(2026, 3, 23, 18, 30), "락 페스티벌");

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("공연 시작 알림");
        assertThat(saved.getMessage()).isEqualTo("2026/03/23 18:30에 락 페스티벌 공연이 시작합니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void 정산_완료_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        notificationService.sendSettlement(1L, LocalDateTime.of(2026, 3, 23, 18, 30), "락 페스티벌",
            BigDecimal.valueOf(100));

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("정산 완료 알림");
        assertThat(saved.getMessage()).isEqualTo("2026/03/23 18:30에 진행한 락 페스티벌 공연의 정산이 완료되었습니다. 정산 금액은 100SSF입니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void 리세일_티켓_판매_완료_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        notificationService.sendResale(1L, "락 페스티벌");

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("2차 티켓 판매 완료");
        assertThat(saved.getMessage()).isEqualTo("락 페스티벌 공연의 2차 티켓 판매가 완료되었습니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void 공연_등록_요청_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        notificationService.sendRequestCreate(1L);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("공연 등록 요청");
        assertThat(saved.getMessage()).isEqualTo("공연 등록 요청 건이 있습니다.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void 공연_수정_요청_알림_생성_성공() {
        int before = notificationRepository.findAll().size();

        notificationService.sendRequestUpdate(1L);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(before + 1);

        Notification saved = notifications.get(notifications.size() - 1);
        assertThat(saved.getTitle()).isEqualTo("공연 수정 요청");
        assertThat(saved.getMessage()).isEqualTo("공연 수정 요청 건이 있습니다.");
        assertThat(saved.isRead()).isFalse();
    }
}
