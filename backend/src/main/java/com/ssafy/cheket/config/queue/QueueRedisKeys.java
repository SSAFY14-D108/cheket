package com.ssafy.cheket.config.queue;

public final class QueueRedisKeys {

    private QueueRedisKeys() {
    }

    // waiting
    public static final String WAIT_PREFIX = "queue:wait:";

    // active 수 관리
    public static final String ACTIVE_PREFIX = "queue:active:";

    // queue token meta
    public static final String TOKEN_PREFIX = "queue:token:";

    // user -> queueToken 역인덱스
    public static final String USER_TOKEN_PREFIX = "queue:user:";

    // join sequence
    public static final String SEQ_PREFIX = "queue:seq:";

    // 현재 queue가 동작 중이 session 목록
    public static final String ACTIVE_SESSIONS_KEY = "queue:sessions:active:";

    // ACTIVE 권한 검증용 key
    public static final String ACTIVE_USER_KEY_PREFIX = "active:";

    // 좌석 페이지 진입 후 access token
    public static final String SEAT_ACCESS_PREFIX = "seat:access:";

    public static String waitKey(Long sessionId) {
        return WAIT_PREFIX + sessionId;
    }

    public static String activeSetKey(Long sessionId) {
        return ACTIVE_PREFIX + sessionId;
    }

    public static String tokenKey(String queueToken) {
        return TOKEN_PREFIX + queueToken;
    }

    public static String userTokenKey(Long sessionId, Long userId) {
        return USER_TOKEN_PREFIX + sessionId + ":" + userId;
    }

    public static String seqKey(Long sessionId) {
        return SEQ_PREFIX + sessionId;
    }

    public static String activeUserKey(Long showId, Long sessionId, Long userId) {
        return ACTIVE_USER_KEY_PREFIX + showId + ":" + sessionId + ":" + userId;
    }

    public static String seatAccessKey(String seatAccessToken) {
        return SEAT_ACCESS_PREFIX + seatAccessToken;
    }

}
