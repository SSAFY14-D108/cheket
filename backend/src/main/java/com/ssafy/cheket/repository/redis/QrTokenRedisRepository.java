package com.ssafy.cheket.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QrTokenRedisRepository {

    // Redis 키 prefix — qr:token:{ticketId} 형태로 저장
    private static final String QR_TOKEN_PREFIX = "qr:token:";

    private final StringRedisTemplate stringRedisTemplate;

    // QR 토큰 저장 — TTL(30초) 후 자동 삭제
    public void save(Long ticketId, String qrToken, Duration ttl) {
        String key = generateKey(ticketId);
        stringRedisTemplate.opsForValue().set(key, qrToken, ttl);
    }

    // QR 토큰 조회 — 없으면 Optional.empty (만료 or 미발급)
    public Optional<String> find(Long ticketId) {
        String key = generateKey(ticketId);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    // QR 토큰 삭제 — 검증 성공 후 일회성 보장을 위해 즉시 삭제
    public void delete(Long ticketId) {
        String key = generateKey(ticketId);
        stringRedisTemplate.delete(key);
    }

    // Redis 키 생성 — ex) "qr:token:42"
    private String generateKey(Long ticketId) {
        return QR_TOKEN_PREFIX + ticketId;
    }
}
