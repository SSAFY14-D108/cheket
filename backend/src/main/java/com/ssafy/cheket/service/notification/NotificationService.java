package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface NotificationService {
    void createNotification(CreateNotificationRequest request);

    void sendShowStart(Long userId, LocalDateTime sessionDateTime, String showTitle);

    void sendResale(Long userId, String showTitle);

    void sendSettlement(Long userId, LocalDateTime sessionDateTime, String showTitle, BigDecimal settlementAmount);

    void sendRequestCreate(Long userId);

    void sendRequestUpdate(Long userId);
}
