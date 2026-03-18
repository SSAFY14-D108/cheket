package com.ssafy.cheket.repository.queue;

import com.ssafy.cheket.config.queue.QueueRedisKeys;
import com.ssafy.cheket.dto.queue.QueueTokenMeta;
import com.ssafy.cheket.enums.QueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class QueueRepository {
    private final StringRedisTemplate redisTemplate;

    // queueToken 조회
    public String getQueueToken(Long userid, Long sessionId) {
        String key = QueueRedisKeys.USER_TOKEN_PREFIX + sessionId + ":" + userid;
        return redisTemplate.opsForValue().get(key);
    }

    // 동일 유저가 동일 회차에 대해 기존 queueToken 을 가지고 있는지 확인
    public boolean hasQueueToken(Long userId, Long sessionId) {
        String key = QueueRedisKeys.userTokenKey(sessionId, userId);
        String queueToken = redisTemplate.opsForValue().get(key);
        return queueToken != null && !queueToken.isBlank();
    }

    // queueToken 저정
    public void saveUserQueueToken(Long userId, Long sessionId, String queueToken) {
        String key = QueueRedisKeys.userTokenKey(sessionId, userId);
        redisTemplate.opsForValue().set(key, queueToken);
    }

    // queueToken 삭제
    public void deleteUserQueueToken(Long userId, Long sessionId) {
        String key = QueueRedisKeys.userTokenKey(sessionId, userId);
        redisTemplate.delete(key);
    }

    // 회차별 대기열 순번 증가 ->
    public Long incrementSequence(Long sessionId) {
        String key = QueueRedisKeys.seqKey(sessionId);
        return redisTemplate.opsForValue().increment(key);
    }

    // waiting queue 에 사용자 등록
    public void addWaiting(Long sessionId, String queueToken, Long joinSeq) {
        String key = QueueRedisKeys.waitKey(sessionId);
        redisTemplate.opsForZSet().add(key, queueToken, joinSeq.doubleValue());
    }

    // waiting queue 에서 사용자 제거
    public void removeWaiting(Long sessionId, String queueToken) {
        String key = QueueRedisKeys.waitKey(sessionId);
        redisTemplate.opsForZSet().remove(key, queueToken);
    }

    // 현재 queueToken 의 waiting 순번 조회
    public Long getWaitingRank(Long sessionId, String queueToken) {
        String key = QueueRedisKeys.waitKey(sessionId);
        return redisTemplate.opsForZSet().rank(key, queueToken);
    }

    // waiting queue 의 맨 앞 queueToken 하나 제거 후 꺼내기
    public String getFirstWaitingToken(Long sessionId) {
        String key = QueueRedisKeys.waitKey(sessionId);
        TypedTuple<String> tokens = redisTemplate.opsForZSet().popMin(key);
        if (tokens == null)
            return null;
        return tokens.getValue();
    }

    // queueTokenMeta 저장
    public void saveQueueMeta(String queueToken, QueueTokenMeta meta) {
        String key = QueueRedisKeys.tokenKey(queueToken);

        redisTemplate.opsForHash().put(key, "userId", String.valueOf(meta.getUserId()));
        redisTemplate.opsForHash().put(key, "showId", String.valueOf(meta.getShowId()));
        redisTemplate.opsForHash().put(key, "sessionId", String.valueOf(meta.getSessionId()));
        redisTemplate.opsForHash().put(key, "status", meta.getStatus().name());
        redisTemplate.opsForHash().put(key, "joinSeq", String.valueOf(meta.getJoinSeq()));
        redisTemplate.opsForHash().put(key, "joinedAt", String.valueOf(meta.getJoinedAt()));

        if (meta.getAdmitExpiresAt() != null) {
            redisTemplate.opsForHash().put(key, "admitExpiresAt", String.valueOf(meta.getAdmitExpiresAt()));
        }
        if (meta.getEnteredAt() != null) {
            redisTemplate.opsForHash().put(key, "enteredAt", String.valueOf(meta.getEnteredAt()));
        }
        if (meta.getLeftAt() != null) {
            redisTemplate.opsForHash().put(key, "leftAt", String.valueOf(meta.getLeftAt()));
        }
    }

    // queueTokenMeta 삭제
    public void deleteQueueTokenMeta(String queueToken) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        redisTemplate.delete(key);
    }

    // queueToken 메타 전체 조회
    public QueueTokenMeta getQueueTokenMeta(String queueToken) {
        String key = QueueRedisKeys.tokenKey(queueToken);

        Object userId = redisTemplate.opsForHash().get(key, "userId");
        Object showId = redisTemplate.opsForHash().get(key, "showId");
        Object sessionId = redisTemplate.opsForHash().get(key, "sessionId");
        Object status = redisTemplate.opsForHash().get(key, "status");
        Object joinSeq = redisTemplate.opsForHash().get(key, "joinSeq");
        Object joinedAt = redisTemplate.opsForHash().get(key, "joinedAt");
        Object admitExpiresAt = redisTemplate.opsForHash().get(key, "admitExpiresAt");
        Object enteredAt = redisTemplate.opsForHash().get(key, "enteredAt");
        Object leftAt = redisTemplate.opsForHash().get(key, "leftAt");

        if (userId == null || showId == null || sessionId == null || status == null || joinSeq == null
            || joinedAt == null) {
            return null;
        }

        return QueueTokenMeta.builder().userId(Long.parseLong(userId.toString()))
            .showId(Long.parseLong(showId.toString())).sessionId(Long.parseLong(sessionId.toString()))
            .status(QueueStatus.valueOf(status.toString())).joinSeq(Long.parseLong(joinedAt.toString()))
            .joinedAt(Long.parseLong(joinedAt.toString()))
            .admitExpiresAt((admitExpiresAt == null) ? null : Long.parseLong(admitExpiresAt.toString()))
            .enteredAt((enteredAt == null) ? null : Long.parseLong(enteredAt.toString()))
            .leftAt((leftAt == null) ? null : Long.parseLong(leftAt.toString())).build();
    }

    // queueToken 의 상태 조회
    public QueueStatus getQueueStatus(String queueToken) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        Object status = redisTemplate.opsForHash().get(key, "status");

        if (status == null)
            return null;
        return QueueStatus.valueOf(status.toString());
    }

    // queueToken 의 상태 변경
    public void updateQueueStatus(String queueToken, QueueStatus status) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        redisTemplate.opsForHash().put(key, "status", status.name());
    }

    // queueToken 의 userId 조회
    public Long getUserId(String queueToken) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        Object userId = redisTemplate.opsForHash().get(key, "userId");
        return (userId == null) ? null : Long.parseLong(userId.toString());
    }

    // ACTIVE 만료 시각 저장
    public void updateAdmitExpiresAt(String queueToken, Long admitExpiresAt) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        redisTemplate.opsForHash().put(key, "admitExpiresAt", String.valueOf(admitExpiresAt));
    }

    // ACTIVE 만료 시각 조회
    public Long getAdmitExpiresAt(String queueToken) {
        String key = QueueRedisKeys.tokenKey(queueToken);
        Object admitExpiresAt = redisTemplate.opsForHash().get(key, "admitExpiresAt");
        return (admitExpiresAt == null) ? null : Long.parseLong(admitExpiresAt.toString());
    }

    // 활성 최차 목록에 sessionId 추가
    public void addActiveSession(Long sessionId) {
        redisTemplate.opsForSet().add(QueueRedisKeys.ACTIVE_SESSIONS_KEY, String.valueOf(sessionId));
    }

    // ACTIVE 권한 key 저장
    public void saveActiveUserKey(Long showId, Long sessionId, Long userId, Long ttlSeconds) {
        String key = QueueRedisKeys.activeUserKey(showId, sessionId, userId);
        redisTemplate.opsForValue().set(key, "ACTIVE", Duration.ofSeconds(ttlSeconds));
    }

    // ACTIVE 권한 key 삭제
    public void deleteActiveUserKey(Long showId, Long sessionId, Long userId) {
        String key = QueueRedisKeys.activeUserKey(showId, sessionId, userId);
        redisTemplate.delete(key);
    }

    // ACTIVE 집합에 queueToken 추가
    public void addActive(Long sessionId, String queueToken) {
        String key = QueueRedisKeys.activeSetKey(sessionId);
        redisTemplate.opsForSet().add(key, queueToken);
    }

    // ACTIVE 집합에서 queueToken 제거
    public void removeActive(Long sessionId, String queueToken) {
        String key = QueueRedisKeys.activeSetKey(sessionId);
        redisTemplate.opsForSet().remove(key, queueToken);
    }

    // 현재 ACTIVE 인원 수 조회
    public Long countActive(Long sessionId) {
        String key = QueueRedisKeys.activeSetKey(sessionId);
        Long size = redisTemplate.opsForSet().size(key);
        return (size == null) ? 0L : size;
    }

    // 특정 queueToken 이 ACTIVE 집합에 있는지 확인
    public boolean isActiveMember(Long sessionId, String queueToken) {
        String key = QueueRedisKeys.activeSetKey(sessionId);
        Boolean result = redisTemplate.opsForSet().isMember(key, queueToken);
        return Boolean.TRUE.equals(result);
    }

}
