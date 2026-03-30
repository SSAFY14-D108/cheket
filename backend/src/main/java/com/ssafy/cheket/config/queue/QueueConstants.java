package com.ssafy.cheket.config.queue;

public final class QueueConstants {

    private QueueConstants() {
    }

    // 회차당 동시에 ACTIVE 상태로 둘 수 있는 최대 인원
    public static final long ACTIVE_LIMIT = 10L;

    // ACTIVE 상태 유지 시간 (초)
    public static final long ACTIVE_TTL_SECONDS = 43200L;

    // queue/enter 이후 좌석 선택 권한 유지 시간 (초)
    public static final long SEAT_ACCESS_TTL_SECONDS = 300L;

    // 프론트 polling 간격 (초)
    public static final int POLLING_INTERVAL_SECONDS = 3;

    // 단순 예상 대기 시간 계산용 값
    public static final long ESTIMATED_WAIT_PER_PERSON_SECONDS = 2L;

    // 토큰 prefix
    public static final String QUEUE_TOKEN_PREFIX = "qtk_";
    public static final String SEAT_ACCESS_TOKEN_PREFIX = "sat_";

}
