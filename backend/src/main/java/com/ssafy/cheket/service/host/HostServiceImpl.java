package com.ssafy.cheket.service.host;

import com.ssafy.cheket.config.jwt.JwtTokenProvider;
import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.host.request.LoginRequest;
import com.ssafy.cheket.dto.host.request.ModifyHostInfoRequest;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.dto.host.response.GetHostInfoResponse;
import com.ssafy.cheket.dto.host.response.LoginResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.exception.common.UnauthorizedException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostServiceImpl implements HostService {

    private final HostRepository hostRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    // 사업자 등록번호 확인 정규식
    private static final String REGEX = "^\\d{3}-\\d{2}-\\d{5}$";

    // 주최측 로그인
    @Override
    public LoginResponse hostLogin(LoginRequest request) {
        // 1. 클라이언트가 보낸 이메일로 해당 유저 조회 - 없으면 404
        Host host = hostRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
        // 2. 비밀번호 검증 - 틀리면 401
        if (!passwordEncoder.matches(request.password(), host.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(host.getId(), host.getEmail(), "HOST");
        String refreshToken = jwtTokenProvider.generateRefreshToken(host.getId(), host.getEmail(), "HOST");

        return new LoginResponse(accessToken, refreshToken);

    }

    // 주최측 회원가입
    @Override
    @Transactional
    public void hostSignup(HostSignupRequest request) throws Exception {
        // 1단계: 이메일 중복 체크
        if (hostRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }
        // 2단계: 사업자등록번호 중복 체크
        if (hostRepository.existsByBusinessNo(request.businessNo())) {
            throw new ConflictException("이미 존재하는 사업자 등록번호입니다.");
        }

        // 3단계: 지갑 생성
        String filename = WalletUtils.generateNewWalletFile(keystorePassword, new File(keystoreDirectory));
        Credentials credentials = WalletUtils.loadCredentials(keystorePassword,
            new File(keystoreDirectory + "/" + filename));
        String address = credentials.getAddress();

        Wallet wallet = Wallet.builder().address(address).keystoreFilename(filename).build();
        walletRepository.save(wallet);

        Host host = Host.builder().walletId(wallet.getId()).companyName(request.companyName())
            .businessNo(request.businessNo()).email(request.email())
            .password(passwordEncoder.encode(request.password())).build();

        hostRepository.save(host);
    }

    // 사업자 등록번호 중복 확인
    @Override
    public CheckBusinessNoDuplicateResponse checkBusinessNoDuplicate(String businessNo) {
        if (!businessNo.matches(REGEX)) {
            throw new BadRequestException("잘못된 사업자 등록번호 형식입니다.");
        }

        if (hostRepository.existsByBusinessNo(businessNo)) {
            throw new ConflictException("이미 등록된 사업자 등록번호입니다.");
        }

        return new CheckBusinessNoDuplicateResponse(false);
    }

    // 회사 정보 조회
    @Override
    public GetHostInfoResponse getHostInfo(Long id) {
        Host host = hostRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        return new GetHostInfoResponse(host.getCompanyName(), host.getBusinessNo(), host.getEmail());
    }

    // 회사 정보 수정
    @Override
    public void modifyHostInfo(Long id, ModifyHostInfoRequest request) {
        Host host = hostRepository.findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
        host.setCompanyName(request.companyName());
        host.setEmail(request.email());
        hostRepository.save(host);
    }

}
