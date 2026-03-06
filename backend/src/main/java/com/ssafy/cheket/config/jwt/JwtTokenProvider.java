package com.ssafy.cheket.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-expiration}") long accessExpiration,
        @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Access Token 설정
    public String generateAccessToken(Long userId, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessExpiration))
            .signWith(secretKey)
            .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshExpiration))
            .signWith(secretKey)
            .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }

    // 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload();
        return claims.get("role", String.class);
    }
}
