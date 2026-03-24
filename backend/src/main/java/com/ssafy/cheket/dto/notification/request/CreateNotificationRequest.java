package com.ssafy.cheket.dto.notification.request;

import com.ssafy.cheket.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class CreateNotificationRequest {
    private Long userId;
    private NotificationType type;
    private String title;
    private Map<String, String> variables;
}
