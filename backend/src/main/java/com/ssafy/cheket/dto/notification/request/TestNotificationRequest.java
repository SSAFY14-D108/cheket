package com.ssafy.cheket.dto.notification.request;

import com.ssafy.cheket.enums.NotificationType;

import java.util.Map;

public record TestNotificationRequest(Long userId, NotificationType type, String title, Map<String, String> variables,
    Map<String, String> data) {
}
