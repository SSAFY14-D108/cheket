package com.ssafy.cheket.repository.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QueueRepository {
    private final StringRedisTemplate redisTemplate;

}
