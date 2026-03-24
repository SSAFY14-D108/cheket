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
}
