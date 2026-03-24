package com.ssafy.cheket.service.user;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.notification.response.GetNotificationsResponse;
import com.ssafy.cheket.dto.user.request.SaveFcmTokenRequest;
import com.ssafy.cheket.dto.user.request.UpdateNotificationRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;
import com.ssafy.cheket.dto.user.response.GetProfileResponse;
import com.ssafy.cheket.entity.notification.Notification;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.*;
import com.ssafy.cheket.repository.notification.NotificationRepository;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;

    private final WalletService walletService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    private final Pattern PHONENUMBER_REGEX = Pattern.compile("^010-\\d{4}-\\d{4}$");

    @Override
    @Transactional
    public void userSignup(UserSignupRequest request) {
        // 1단계: 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }

        // 1.5단계: 전화번호 정규식 확인
        if (!PHONENUMBER_REGEX.matcher(request.phoneNumber()).matches()) {
            throw new BadRequestException("전화번호의 형식이 올바르지 않습니다. 예) 010-1234-5678");
        }

        // 2단계: 지갑 생성
        try {
            String filename = WalletUtils.generateNewWalletFile(keystorePassword, new File(keystoreDirectory));
            Credentials credentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + filename));

            String address = credentials.getAddress();

            Wallet wallet = Wallet.builder().address(address).keystoreFilename(filename).build();
            walletRepository.save(wallet);

            User user = User.builder().walletId(wallet.getId()).username(request.username())
                .phoneNumber(request.phoneNumber()).email(request.email())
                .password(passwordEncoder.encode(request.password())).notificationEnable(true).build();

            userRepository.save(user);

            // 3단계: 플랫폼 지갑 → 신규 유저 지갑으로 초기 SSF 전송 (비동기)
            walletService.transferInitialFunds(address, user.getId());
        } catch (Exception e) {
            throw new BlockchainException("지갑 생성 실패: " + e.getMessage());
        }
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

    // FCM 토큰 저장
    @Override
    @Transactional
    public void saveFcmToken(Long userId, SaveFcmTokenRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if (request.fcmToken() == null || request.fcmToken().isBlank())
            throw new BadRequestException("FCM 토큰은 비어 있을 수 없습니다.");

        user.setFcmToken(request.fcmToken());
    }

    // 알림 내역 조회
    @Override
    @Transactional(readOnly = true)
    public List<GetNotificationsResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(notification -> new GetNotificationsResponse(notification.getId(), notification.getMessage(),
                notification.getType().name(), notification.isRead(), notification.getShowId()))
            .toList();
    }

    // 알림 읽음 처리
    @Override
    @Transactional
    public void readNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException("존재하지 않는 알림입니다."));

        if (!notification.getUserId().equals(userId))
            throw new ForbiddenException("해당 알림에 접근할 수 없습니다.");

        notification.setRead(true);
    }
}
