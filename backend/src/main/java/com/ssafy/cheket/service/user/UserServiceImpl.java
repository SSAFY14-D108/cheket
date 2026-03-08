package com.ssafy.cheket.service.user;

import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
    }

    // 이메일 찾기
    @Override
    public FindEmailResponse findEmail(FindEmailRequest request) {
        User user = userRepository.findByUsernameAndPhoneNumber(request.username(), request.phoneNumber())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
        return new FindEmailResponse(user.getEmail());
    }

}
