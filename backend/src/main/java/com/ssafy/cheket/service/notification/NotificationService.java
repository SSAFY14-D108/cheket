package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;

public interface NotificationService {
    void createNotification(CreateNotificationRequest request);
}
