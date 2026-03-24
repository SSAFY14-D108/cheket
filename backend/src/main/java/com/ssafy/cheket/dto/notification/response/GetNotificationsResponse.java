package com.ssafy.cheket.dto.notification.response;

public record GetNotificationsResponse(Long id, String message, String type, boolean read, Long showId) {
}
