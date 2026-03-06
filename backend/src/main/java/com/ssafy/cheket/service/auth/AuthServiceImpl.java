package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.dto.auth.request.SignupRequest;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }

        // TODO: Web3j WalletUtils로 교체 예정
        String tempAddress = "0x" + UUID.randomUUID().toString().replace("-", "");
        String tempFilename = "temp-" + UUID.randomUUID() + ".json";

        Wallet wallet = Wallet.builder()
                .address(tempAddress)
                .keystoreFilename(tempFilename)
                .build();
        walletRepository.save(wallet);

        User user = User.builder()
                .walletId(wallet.getId())
                .username(request.username())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .notificationEnable(true)
                .build();
        userRepository.save(user);
    }
}
