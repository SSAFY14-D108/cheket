package com.ssafy.cheket.repository.queue;

import com.ssafy.cheket.config.queue.QueueRedisKeys;
import com.ssafy.cheket.dto.queue.QueueTokenMeta;
import com.ssafy.cheket.enums.QueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class QueueRepository {
    private final StringRedisTemplate redisTemplate;

    // queueToken에 해당하는 Redis hash를 조회해서 메타 정보를 반환
    public QueueTokenMeta findQueueTokenMeta(String queueToken) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(QueueRedisKeys.tokenKey(queueToken));

        if (entries.isEmpty())
            return null;

        return QueueTokenMeta.builder().userId(parseLong(entries.get("userId")))
            .showId(parseLong(entries.get("showId"))).sessionId(parseLong(entries.get("sessionId")))
            .status(parseQueueStatus(entries.get("status"))).joinSeq(parseLong(entries.get("joinSeq")))
            .joinedAt(parseLong(entries.get("joinedAt"))).admitExpiresAt(parseLong(entries.get("admitExpiresAt")))
            .enteredAt(parseLong(entries.get("enteredAt"))).leftAt(parseLong(entries.get("leftAt"))).build();
    }

    // WAITING ZSet에서 해당 토큰 제거
    public void removeFromWaitingQueue(Long sessionId, String queueToken) {
        redisTemplate.opsForZSet().remove(QueueRedisKeys.waitKey(sessionId), queueToken);
    }

    // ACTIVE ZSet에서 해당 토큰 제거
    public void removeFromActiveSet(Long sessionId, String queueToken) {
        redisTemplate.opsForZSet().remove(QueueRedisKeys.activeSetKey(sessionId), queueToken);
    }

    // 활성 사용자 중복 진입 방지용 key 삭제
    public void deleteActiveUserKey(Long showId, Long sessionId, Long userId) {
        redisTemplate.delete(QueueRedisKeys.activeUserKey(showId, sessionId, userId));
    }

    // 토큰 상태값 변경
    public void updateStatus(String queueToken, QueueStatus status) {
        redisTemplate.opsForHash().put(QueueRedisKeys.tokenKey(queueToken), "status", status.name());
    }

    // 대기열 이탈 시각 저장
    public void updateLeftAt(String queueToken, Long leftAt) {
        redisTemplate.opsForHash().put(QueueRedisKeys.tokenKey(queueToken), "leftAt", String.valueOf(leftAt));
    }

    // ACTIVE 입장 만료 시각 저장
    public void updateAdmitExpiresAt(String queueToken, Long admitExpiresAt) {
        redisTemplate.opsForHash().put(QueueRedisKeys.tokenKey(queueToken), "admitExpiresAt",
            String.valueOf(admitExpiresAt));
    }

    // sessionId + userId 기준 토큰 매핑 삭제
    public void deleteUserTokenMapping(Long sessionId, Long userId) {
        redisTemplate.delete(QueueRedisKeys.userTokenKey(sessionId, userId));
    }

    // 이미 ACTIVE 상태인 사용자인지 확인
    public boolean existsActiveUserKey(Long showId, Long sessionId, Long userId) {
        return redisTemplate.hasKey(QueueRedisKeys.activeUserKey(showId, sessionId, userId));
    }

    // 현재 ACTIVE 인원 수 조회
    public Long getActiveCount(Long sessionId) {
        Long count = redisTemplate.opsForZSet().zCard(QueueRedisKeys.activeSetKey(sessionId));
        return count == null ? 0L : count;
    }

    // WAITING ZSet에서 가장 앞 순번 토큰을 하나 꺼내고 제거
    public String pollNextWaitingToken(Long sessionId) {
        Set<String> values = redisTemplate.opsForZSet().range(QueueRedisKeys.waitKey(sessionId), 0, 0);

        if (values == null || values.isEmpty()) {
            return null;
        }

        String queueToken = values.iterator().next();
        redisTemplate.opsForZSet().remove(QueueRedisKeys.waitKey(sessionId), queueToken);
        return queueToken;
    }

    // ACTIVE ZSet에 토큰 추가 (score는 입장 만료 시각)
    public void addToActiveSet(Long sessionId, String queueToken, Long admitExpiresAt) {
        redisTemplate.opsForZSet().add(QueueRedisKeys.activeSetKey(sessionId), queueToken,
            admitExpiresAt.doubleValue());
    }

    // 활성 사용자 key 저장
    public void saveActiveUserKey(Long showId, Long sessionId, Long userId, long ttlSeconds) {
        redisTemplate.opsForValue().set(QueueRedisKeys.activeUserKey(showId, sessionId, userId), "1",
            Duration.ofSeconds(ttlSeconds));
    }

    private Long parseLong(Object value) {
        if (value == null)
            return null;
        return Long.parseLong(String.valueOf(value));
    }

    private QueueStatus parseQueueStatus(Object value) {
        if (value == null)
            return null;
        return QueueStatus.valueOf(String.valueOf(value));
    }
}
