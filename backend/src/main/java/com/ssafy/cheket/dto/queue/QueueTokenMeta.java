package com.ssafy.cheket.dto.queue;

import com.ssafy.cheket.enums.QueueStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QueueTokenMeta {

    private Long id;
    private Long showId;
    private Long sessionId;
    private QueueStatus status;

    // queue 진입 시각
    private LocalDateTime joinedAt;

    // ACTIVE 만료 시각
    private LocalDateTime admitExpiredAt;

    // queue / enter 성공 시각
    private LocalDateTime enteredAt;

    // 사용자 이탈 시각
    private LocalDateTime leftAt;

}
