package com.ssafy.cheket.service.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendPush(String fcmToken, String title, String body) {
        try {
            Message message = Message.builder().setToken(fcmToken)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build()).putData("title", title)
                .putData("body", body).build();

            String response = firebaseMessaging.send(message);
            log.info("FCM sent successfully: {}", response);
        } catch (Exception e) {
            log.error("FCM send failed. token={}", fcmToken, e);
        }
    }
}
