package com.ssafy.cheket.dto.queue.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record QueueSeatEnterResponse(String seatAccessToken, LocalDateTime seatAccessExpiresAt) {
}
