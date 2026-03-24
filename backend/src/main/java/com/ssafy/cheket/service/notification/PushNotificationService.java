package com.ssafy.cheket.service.notification;

import java.util.Map;

public interface PushNotificationService {
    void sendPush(String fcmToken, String title, String body, Map<String, String> data);
}
