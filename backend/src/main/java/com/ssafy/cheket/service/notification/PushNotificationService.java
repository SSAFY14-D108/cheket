package com.ssafy.cheket.service.notification;

public interface PushNotificationService {
    void sendPush(String fcmToken, String title, String body);
}
