package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public interface NotificationService {
    void createNotification(CreateNotificationRequest request);

    void createNotification(CreateNotificationRequest request, Map<String, String> data);

    void sendShowStart(Long userId, LocalDateTime sessionDateTime, String showTitle);

    void sendResale(Long userId, String showTitle);

    void sendSettlement(Long userId, LocalDateTime sessionDateTime, String showTitle, BigDecimal settlementAmount);

    void sendRequestCreate(Long userId, Long showId);

    void sendRequestUpdate(Long userId, Long showId);

    void sendTomorrowShowStartNotifications();

    void sendTodayShowStartNotifications();
}
