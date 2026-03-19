package com.ssafy.cheket.dto.queue.response;

import com.ssafy.cheket.enums.QueueStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record QueueInfoResponse(QueueStatus status, Long position, Long aheadCount, Long estimatedWaitSeconds,
    LocalDateTime admitExpiresAt) {
}
