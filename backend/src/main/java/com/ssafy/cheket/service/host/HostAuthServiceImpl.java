package com.ssafy.cheket.service.host;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.host.request.LoginRequest;
import com.ssafy.cheket.dto.host.response.LoginResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.exception.common.UnauthorizedException;
import com.ssafy.cheket.repository.host.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostAuthServiceImpl implements HostAuthService {

    private final HostRepository hostRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 주최측 로그인
    @Override
    public LoginResponse hostLogin(LoginRequest request) {
        // 1. 클라이언트가 보낸 이메일로 해당 유저 조회 - 없으면 404
        Host host = hostRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("이메일 또는 비밀번호가 일치하지 않습니다."));
        // 2. 비밀번호 검증 - 틀리면 401
        if (!passwordEncoder.matches(request.password(), host.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(host.getId(), host.getEmail(), "HOST");
        String refreshToken = jwtTokenProvider.generateRefreshToken(host.getId(), host.getEmail(), "HOST");

        return new LoginResponse(accessToken, refreshToken);

    }

}
