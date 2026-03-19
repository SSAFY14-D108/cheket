package com.ssafy.cheket.service.queue;

import com.ssafy.cheket.dto.queue.QueueTokenMeta;
import com.ssafy.cheket.enums.QueueStatus;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.queue.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ssafy.cheket.config.queue.QueueConstants.ACTIVE_LIMIT;
import static com.ssafy.cheket.config.queue.QueueConstants.ACTIVE_TTL_SECONDS;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {
    private final QueueRepository queueRepository;

    @Transactional
    @Override
    public void leaveQueue(Long userId, Long showId, Long sessionId, String queueToken) {
        QueueTokenMeta meta = queueRepository.findQueueTokenMeta(queueToken);

        validateQueueToken(meta, userId, showId, sessionId);

        QueueStatus status = meta.getStatus();

        if (status == QueueStatus.LEFT || status == QueueStatus.EXPIRED)
            return;

        if (status == QueueStatus.WAITING) { // WAITING 상태면 대기열에서 제거
            queueRepository.removeFromWaitingQueue(sessionId, queueToken);
        } else if (status == QueueStatus.ACTIVE) { // ACTIVE 상태면 활성 사용자 key와 active set에서 제거
            queueRepository.deleteActiveUserKey(showId, sessionId, userId);
            queueRepository.removeFromActiveSet(sessionId, queueToken);
        } else if (status == QueueStatus.COMPLETED) { // 이미 좌석 선택 페이지에 진입한 사용자는 이탈 불가
            throw new ConflictException("이미 좌석 선택 페이지에 진입한 사용자는 대기열 이탈 대상이 아닙니다.");
        }

        // 토큰 상태를 LEFT로 바꾸고 이탈 시각 기록
        queueRepository.updateStatus(queueToken, QueueStatus.LEFT);
        queueRepository.updateLeftAt(queueToken, currentEpochSec());

        // 사용자-토큰 매핑 삭제
        queueRepository.deleteUserTokenMapping(sessionId, userId);

        // 빈 ACTIVE 자리가 생겼다면 WAITING 사용자를 승급
        promoteWaitingToActive(sessionId);
    }

    private void promoteWaitingToActive(Long sessionId) {
        long currentActiveCount = queueRepository.getActiveCount(sessionId);
        long availableSlots = ACTIVE_LIMIT - currentActiveCount;

        if (availableSlots <= 0)
            return;

        // 비어 있는 자리 수만큼 WAITING -> ACTIVE
        for (int i = 0; i < availableSlots; i++) {
            String nextQueueToken = queueRepository.pollNextWaitingToken(sessionId);

            if (nextQueueToken == null)
                return;

            QueueTokenMeta meta = queueRepository.findQueueTokenMeta(nextQueueToken);
            if (meta == null)
                continue;

            if (meta.getStatus() != QueueStatus.WAITING)
                continue;

            long admitExpiresAt = currentEpochSec() + ACTIVE_TTL_SECONDS;

            queueRepository.updateStatus(nextQueueToken, QueueStatus.ACTIVE);
            queueRepository.updateAdmitExpiresAt(nextQueueToken, admitExpiresAt);
            queueRepository.saveActiveUserKey(meta.getShowId(), meta.getSessionId(), meta.getUserId(),
                ACTIVE_TTL_SECONDS);
            queueRepository.addToActiveSet(meta.getSessionId(), nextQueueToken, admitExpiresAt);
        }
    }

    private void validateQueueToken(QueueTokenMeta meta, Long userId, Long showId, Long sessionId) {
        if (meta == null)
            throw new NotFoundException("유효하지 않은 queueToken 입니다.");

        if (!meta.getUserId().equals(userId))
            throw new ForbiddenException("queueToken 소유자와 요청 사용자가 일치하지 않습니다.");

        if (!meta.getShowId().equals(showId) || !meta.getSessionId().equals(sessionId))
            throw new ConflictException("요청 정보와 queueToken 정보가 일치하지 않습니다.");
    }

    private long currentEpochSec() {
        return System.currentTimeMillis() / 1000;
    }
}
