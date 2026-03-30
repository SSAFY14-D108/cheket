package com.ssafy.cheket.config.queue;

import java.util.UUID;

public final class QueueTokenGenerator {

    private QueueTokenGenerator() {
    }

    public static String generateQueueToken() {
        return QueueConstants.QUEUE_TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateSeatAccessToken() {
        return QueueConstants.SEAT_ACCESS_TOKEN_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

}
