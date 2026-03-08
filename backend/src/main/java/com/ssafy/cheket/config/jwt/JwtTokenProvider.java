package com.ssafy.cheket.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component // Spring이 자동으로 빈 등록 -> 다른 클래스에서 주입받아 사용 가능
public class JwtTokenProvider {
    private final SecretKey secretKey; // 토큰 서명/검증에 쓰는 비밀 키
    private final long accessExpiration; // Access Token 만료시간 (ms)
    private final long refreshExpiration; // Refresh Token 만료시간 (ms)

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, // .env의 JWT_SECRET
        @Value("${jwt.access-expiration}") long accessExpiration, // .env의 JWT_ACCESS_EXPIRATION
        @Value("${jwt.refresh-expiration}") long refreshExpiration // .env의 JWT_REFRESH_EXPIRATION
    ) {
        // 문자열 비밀키를 HMAC-SHA 알고리즘용 SecretKey 객체로 변환
        // 이 문자열로는 바로 서명 못함 -> SecretKey 객체로 변환해야 함
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Access Token 생성 — API 요청 인증용 (만료: 30분)
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(userId)) // 토큰 주인 (userId)
            .claim("email", email) // 추가 정보: 이메일 (DB 조회 없이 사용 가능)
            .claim("role", role) // 추가 정보: 권한 (USER/HOST 구분)
            .issuedAt(now) // 토큰 발급 시간
            .expiration(new Date(now.getTime() + accessExpiration)) // 만료 시간
            .signWith(secretKey) // 비밀 키로 서명 (위조 방지)
            .compact(); // 최종 JWT 문자열 생성
    }

    // Refresh Token 생성 — Access Token 재발급용 (만료: 7일)
    // 재발급 시 DB 조회 없이 새 Access Token을 만들 수 있도록 email, role도 담음
    public String generateRefreshToken(Long userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }


    // 토큰 유효성 검증 — 서명 위조, 만료 여부 확인
    public boolean validateToken(String token) {
        try {
            // 비밀 키로 서명을 검증하고 파싱 시도
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true; // 성공하면 유효한 토큰
        } catch (Exception e) {
            return false; // 서명 불일치, 만료, 형식 오류 등 → 유효하지 않음
        }
    }

    // 토큰에서 userId 추출 — subject에 저장한 값을 꺼냄
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    // 토큰에서 role 추출 — claim에 저장한 값을 꺼냄
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }

    // 토큰에서 email 추출
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
        return claims.get("email", String.class);
    }

}
