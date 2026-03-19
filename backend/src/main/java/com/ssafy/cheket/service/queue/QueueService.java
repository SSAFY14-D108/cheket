package com.ssafy.cheket.service.queue;

public interface QueueService {
    void leaveQueue(Long userId, Long showId, Long sessionId, String queueToken);
}
