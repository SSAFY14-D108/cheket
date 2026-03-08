package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.auth.request.LoginRequest;
import com.ssafy.cheket.dto.auth.response.LoginResponse;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.exception.common.UnauthorizedException;
import com.ssafy.cheket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Spring이 빈으로 등록해야 함
@RequiredArgsConstructor // final 필드 생성자
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken);
    }
}
