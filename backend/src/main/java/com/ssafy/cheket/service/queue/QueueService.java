package com.ssafy.cheket.service.queue;

import com.ssafy.cheket.dto.queue.response.QueueEnterResponse;
import com.ssafy.cheket.dto.queue.response.QueueInfoResponse;
import com.ssafy.cheket.dto.queue.response.QueueSeatEnterResponse;

public interface QueueService {

    // 대기열 진입
    QueueEnterResponse enterQueue(Long userId, Long showId, Long sessionId);

    // 대기열 정보 조회
    QueueInfoResponse getQueueInfo(Long userId, Long showId, Long sessionId, String queueToken);

    // 대기열 이탈
    void leaveQueue(Long userId, Long showId, Long sessionId, String queueToken);

    // 좌석 선택 진입
    QueueSeatEnterResponse enterSeatSelection(Long userId, Long showId, Long sessionId, String queueToken);
}
