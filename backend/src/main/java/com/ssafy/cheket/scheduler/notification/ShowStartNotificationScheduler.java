package com.ssafy.cheket.scheduler.notification;

import com.ssafy.cheket.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowStartNotificationScheduler {
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void sendTomorrowShowStartNotifications() {
        notificationService.sendTomorrowShowStartNotifications();
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendTodayShowStartNotifications() {
        notificationService.sendTodayShowStartNotifications();
    }
}
