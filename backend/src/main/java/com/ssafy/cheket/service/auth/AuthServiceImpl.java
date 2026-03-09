package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.auth.request.LoginRequest;
import com.ssafy.cheket.dto.auth.request.ReissueRequest;
import com.ssafy.cheket.dto.auth.response.LoginResponse;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.exception.common.UnauthorizedException;
import com.ssafy.cheket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service // Spring이 빈으로 등록해야 함
@RequiredArgsConstructor // final 필드 생성자
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    // 키와 값이 둘 다 String인 Redis 템플릿 = StringRedisTemplate
    private final StringRedisTemplate redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 클라이언트가 보낸 이메일로 해당 유저 조회 - 없으면 401
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다."));
        // 2. 비밀번호 검증 - 틀리면 401
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), "USER");

        return new LoginResponse(accessToken, refreshToken);
    }

    @Override
    public LoginResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        // 1. Refresh Token 유효성 검증 - 만료되었거나 위조된 토큰이면 401
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 정보 추출 — DB 조회 없이 토큰에서 바로 꺼냄
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        // 3. 새 토큰 발급 (Refresh Token Rotation — 둘 다 새로 발급)
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String accessToken) {
        // Access Token 남은 시간 뒤에 자동 삭제되도록 (블랙리스트에 쌓임 방지)
        long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
        // 값 저장 (TTL 포함) set("키", "값", 만료시간, 시간단위)
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
    }
}
