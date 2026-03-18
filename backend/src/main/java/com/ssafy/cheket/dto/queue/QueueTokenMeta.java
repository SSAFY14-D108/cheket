package com.ssafy.cheket.dto.queue;

import com.ssafy.cheket.enums.QueueStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueueTokenMeta {

    private Long userId;
    private Long showId;
    private Long sessionId;
    private QueueStatus status;

    // 대기열 순번
    private Long joinSeq;

    // queue 진입 시각(epoch sec)
    private Long joinedAt;

    // ACTIVE 만료 시각(epoch sec)
    private Long admitExpiresAt;

    // /queue/enter 성공 시각(epoch sec)
    private Long enteredAt;

    // 사용자 이탈 시각(epoch sec)
    private Long leftAt;
}
