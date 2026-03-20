package com.ssafy.cheket.repository.queue;

import com.ssafy.cheket.config.queue.QueueRedisKeys;
import com.ssafy.cheket.dto.queue.QueueTokenMeta;
import com.ssafy.cheket.dto.queue.SeatAccessMeta;
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

    // queueToken 조회
    public String getQueueToken(Long userId, Long sessionId) {
        return redisTemplate.opsForValue().get(QueueRedisKeys.userTokenKey(sessionId, userId));
    }

    // 동일 유저가 동일 회차에 대해 기존 queueToken 을 가지고 있는지 확인
    public boolean hasQueueToken(Long userId, Long sessionId) {
        String queueToken = redisTemplate.opsForValue().get(QueueRedisKeys.userTokenKey(sessionId, userId));
        return queueToken != null && !queueToken.isBlank();
    }

    // queueToken 저정
    public void saveUserQueueToken(Long userId, Long sessionId, String queueToken) {
        redisTemplate.opsForValue().set(QueueRedisKeys.userTokenKey(sessionId, userId), queueToken);
    }

    // 회차별 대기열 순번 증가
    public Long incrementSequence(Long sessionId) {
        return redisTemplate.opsForValue().increment(QueueRedisKeys.seqKey(sessionId));
    }

    // waiting queue 에 사용자 등록
    public void addWaiting(Long sessionId, String queueToken, Long joinSeq) {
        redisTemplate.opsForZSet().add(QueueRedisKeys.waitKey(sessionId), queueToken, joinSeq.doubleValue());
    }

    // 현재 queueToken 의 waiting 순번 조회
    public Long getWaitingRank(Long sessionId, String queueToken) {
        return redisTemplate.opsForZSet().rank(QueueRedisKeys.waitKey(sessionId), queueToken);
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
        redisTemplate.delete(QueueRedisKeys.tokenKey(queueToken));
    }

    // queueToken 의 상태 조회
    public QueueStatus getQueueStatus(String queueToken) {
        Object status = redisTemplate.opsForHash().get(QueueRedisKeys.tokenKey(queueToken), "status");

        if (status == null)
            return null;
        return QueueStatus.valueOf(status.toString());
    }

    // queueToken 의 userId 조회
    public Long getUserId(String queueToken) {
        Object userId = redisTemplate.opsForHash().get(QueueRedisKeys.tokenKey(queueToken), "userId");
        return (userId == null) ? null : parseLong(userId.toString());
    }

    // ACTIVE 만료 시각 조회
    public Long getAdmitExpiresAt(String queueToken) {
        Object admitExpiresAt = redisTemplate.opsForHash().get(QueueRedisKeys.tokenKey(queueToken), "admitExpiresAt");
        return (admitExpiresAt == null) ? null : parseLong(admitExpiresAt.toString());
    }

    // 활성 회차 목록에 sessionId 추가
    public void addActiveSession(Long sessionId) {
        redisTemplate.opsForSet().add(QueueRedisKeys.ACTIVE_SESSIONS_KEY, String.valueOf(sessionId));
    }

    // 특정 queueToken 이 ACTIVE 집합에 있는지 확인
    public boolean isActiveMember(Long sessionId, String queueToken) {
        Long rank = redisTemplate.opsForZSet().rank(QueueRedisKeys.activeSetKey(sessionId), queueToken);
        return rank != null;
    }

    // queueToken에 해당하는 Redis hash를 조회해서 메타 정보를 반환
    public QueueTokenMeta findQueueTokenMeta(String queueToken) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(QueueRedisKeys.tokenKey(queueToken));

        if (entries.isEmpty())
            return null;

        if (entries.get("userId") == null || entries.get("showId") == null || entries.get("sessionId") == null
            || entries.get("status") == null || entries.get("joinSeq") == null || entries.get("joinedAt") == null)
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

    // 좌석 선택 화면 진입 시각 저장
    public void updateEnteredAt(String queueToken, Long enteredAt) {
        redisTemplate.opsForHash().put(QueueRedisKeys.tokenKey(queueToken), "enteredAt", String.valueOf(enteredAt));
    }

    public void saveSeatAccessToken(String seatAccessToken, Long userId, Long showId, Long sessionId, long ttlSeconds) {
        String key = QueueRedisKeys.seatAccessKey(seatAccessToken);

        redisTemplate.opsForHash().put(key, "userId", String.valueOf(userId));
        redisTemplate.opsForHash().put(key, "showId", String.valueOf(showId));
        redisTemplate.opsForHash().put(key, "sessionId", String.valueOf(sessionId));
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
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

    // seatAccessToken 삭제
    public void deleteSeatAccessToken(String seatAccessToken) {
        redisTemplate.delete(QueueRedisKeys.seatAccessKey(seatAccessToken));
    }

    // seatAccessToken 조회
    public SeatAccessMeta findSeatAccessTokenMeta(String seatAccessToken) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(QueueRedisKeys.seatAccessKey(seatAccessToken));

        if (entries.isEmpty())
            return null;

        if (entries.get("userId") == null || entries.get("showId") == null || entries.get("sessionId") == null)
            return null;

        return SeatAccessMeta.builder().userId(parseLong(entries.get("userId")))
            .showId(parseLong(entries.get("showId"))).sessionId(parseLong(entries.get("sessionId"))).build();
    }
}
