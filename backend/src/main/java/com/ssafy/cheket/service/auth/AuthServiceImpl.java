package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.dto.auth.request.SignupRequest;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // final 필드들의 생성자를 자동으로 생성
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }

        Wallet wallet = Wallet.builder()
                .address("임시지갑주소")
                .keystoreFilename("임시파일명")
                .build();
        walletRepository.save(wallet);

        User user = User.builder()
                .walletId(wallet.getId())
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .password(request.password())
                .notificationEnable(true)
                .build();
        userRepository.save(user);
    }
}
