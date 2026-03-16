package com.ssafy.cheket.service.user;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.user.request.UpdateNotificationRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;
import com.ssafy.cheket.dto.user.response.GetProfileResponse;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.wallet.WalletService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    @Override
    @Transactional
    public void userSignup(UserSignupRequest request) throws Exception {
        // 1단계: 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }

        // 2단계: 지갑 생성
        String filename = WalletUtils.generateNewWalletFile(keystorePassword, new File(keystoreDirectory));
        Credentials credentials = WalletUtils.loadCredentials(keystorePassword,
            new File(keystoreDirectory + "/" + filename));

        String address = credentials.getAddress();

        Wallet wallet = Wallet.builder().address(address).keystoreFilename(filename).build();
        walletRepository.save(wallet);

        User user = User.builder().walletId(wallet.getId()).username(request.username())
            .phoneNumber(request.phoneNumber()).email(request.email())
            .password(passwordEncoder.encode(request.password())).notificationEnable(true).build(); // 최종 객체 생성

        userRepository.save(user);

        // 3단계: 플랫폼 지갑 → 신규 유저 지갑으로 초기 SSF 전송 (비동기)
        // user.getId()를 전달 → Transaction 레코드의 buyerId에 들어감
        // 유저는 거래 주체이므로 Transaction DB에 기록해야 함
        walletService.transferInitialFunds(address, user.getId());
    }

    // 이메일 찾기
    @Override
    public FindEmailResponse findEmail(FindEmailRequest request) {
        User user = userRepository
            .findByUsernameAndPhoneNumberAndDeletedAtIsNull(request.username(), request.phoneNumber())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
        return new FindEmailResponse(user.getEmail());
    }

    // 탈퇴
    @Override
    @Transactional
    public void withdrawUser(Long userId, String accessToken, String refreshToken) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
        user.setDeletedAt(LocalDateTime.now());

        // Access Token 블랙리스트
        long expiration = jwtTokenProvider.getRemainingExpiration(accessToken);
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "withdraw", expiration, TimeUnit.MILLISECONDS);

        // Refresh Token도 블랙리스트 등록
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            long refreshExpiration = jwtTokenProvider.getRemainingExpiration(refreshToken);
            redisTemplate.opsForValue().set("blacklist:" + refreshToken, "withdraw", refreshExpiration,
                TimeUnit.MILLISECONDS);
        }
    }

    // 프로필 조회
    @Override
    public GetProfileResponse getProfile(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        return new GetProfileResponse(user.getId(), user.getUsername(), user.getPhoneNumber(), user.getEmail());
    }

    // 알림 여부 수정
    @Override
    @Transactional
    public void updateNotification(Long userId, UpdateNotificationRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        user.setNotificationEnable(request.notificationEnable());
    }
}
