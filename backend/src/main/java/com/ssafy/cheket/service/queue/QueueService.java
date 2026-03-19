package com.ssafy.cheket.service.queue;

import com.ssafy.cheket.dto.queue.response.QueueEnterResponse;

public interface QueueService {

    // 대기열 진입
    QueueEnterResponse enterQueue(Long userId, Long showId, Long sessionId);

    void leaveQueue(Long userId, Long showId, Long sessionId, String queueToken);
}
