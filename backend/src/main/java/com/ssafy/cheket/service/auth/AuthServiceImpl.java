package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.auth.request.LoginRequest;
import com.ssafy.cheket.dto.auth.request.ReissueRequest;
import com.ssafy.cheket.dto.auth.response.LoginResponse;
import com.ssafy.cheket.dto.auth.response.SearchUserResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.UserType;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.exception.common.UnauthorizedException;
import com.ssafy.cheket.repository.auth.AuthRepository;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service // Spring이 빈으로 등록해야 함
@RequiredArgsConstructor // final 필드 생성자
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    // 키와 값이 둘 다 String인 Redis 템플릿 = StringRedisTemplate
    private final StringRedisTemplate redisTemplate;
    private final AuthRepository authRepository;
    private final SmsService smsService;
    private final HostRepository hostRepository;

    private static final Pattern HOST_REGEX = Pattern.compile("^\\d{3}-\\d{2}-\\d{5}$");
    private static final Pattern USER_REGEX = Pattern.compile("^010\\d{8}$");

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 클라이언트가 보낸 이메일로 해당 유저 조회 - 없으면 401
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
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

        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. 블랙리스트 확인 (탈퇴된 토큰 차단)
        Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + refreshToken);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            throw new UnauthorizedException("이미 탈퇴하거나 로그아웃된 사용자입니다.");
        }

        // 3. Refresh Token에서 정보 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        // 4. 새 토큰 발급 전에 이전 Refresh Token 블랙리스트 등록
        long oldRefreshExpiration = jwtTokenProvider.getRemainingExpiration(refreshToken);
        redisTemplate.opsForValue().set("blacklist:" + refreshToken, "rotated", oldRefreshExpiration,
            TimeUnit.MILLISECONDS);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // Access Token 블랙리스트 등록
        long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "logout", expiration, TimeUnit.MILLISECONDS);

        // Refresh Token 블랙리스트 등록
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            long refreshExpiration = jwtTokenProvider.getRemainingExpiration(refreshToken);
            redisTemplate.opsForValue().set("blacklist:" + refreshToken, "logout", refreshExpiration,
                TimeUnit.MILLISECONDS);
        }
    }

    // 이메일 중복 확인
    @Override
    public void checkEmailDuplicated(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일은 필수입니다.");
        }

        if (authRepository.existsByEmail(email)) {
            throw new ConflictException("이미 존재하는 이메일입니다.");
        }
    }

    // 비밀번호 초기화
    @Override
    public void resetPassword(String phoneNumber, String code, String newPassword) {
        smsService.verifySmsCode(phoneNumber, code);

        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("새 비밀번호는 필수입니다.");
        }

        User user = authRepository.findByPhoneNumberAndDeletedAtIsNull(phoneNumber)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 비밀번호 변경
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new BadRequestException("현재 비밀번호는 필수입니다.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new BadRequestException("새 비밀번호는 필수입니다.");
        }

        User user = authRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BadRequestException("새 비밀번호는 현재 비밀번호와 달라야합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 회원 검색
    @Override
    public SearchUserResponse searchUser(String userType, String number) {
        UserType type;

        try {
            type = UserType.valueOf(userType.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("유효하지 않은 요청입니다.");
        }

        switch (type) {
            case USER -> {
                if (!USER_REGEX.matcher(number).matches()) {
                    throw new BadRequestException("전화번호의 형식이 올바르지 않습니다. 예) 01012345678");
                }

                User user = userRepository.findByPhoneNumberAndDeletedAtIsNull(number)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
                return new SearchUserResponse(user.getId(), user.getUsername(), user.getPhoneNumber());
            }
            case HOST -> {
                if (!HOST_REGEX.matcher(number).matches()) {
                    throw new BadRequestException("사업자 등록번호의 형식이 올바르지 않습니다. 예) 123-45-67890");
                }

                Host host = hostRepository.findByBusinessNo(number)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
                return new SearchUserResponse(host.getId(), host.getCompanyName(), host.getBusinessNo());
            }
            default -> throw new BadRequestException("유효하지 않은 요청입니다.");
        }
    }

}
