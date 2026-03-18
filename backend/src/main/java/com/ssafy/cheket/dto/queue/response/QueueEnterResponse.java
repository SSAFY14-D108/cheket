package com.ssafy.cheket.dto.queue.response;

import com.ssafy.cheket.enums.QueueStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record QueueEnterResponse(String queueToken, QueueStatus status, Long position, Long aheadCount,
    Long estimatedWaitSeconds, Integer pollingIntervalSeconds, LocalDateTime admitExpiresAt) {
}
