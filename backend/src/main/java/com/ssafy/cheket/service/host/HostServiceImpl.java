package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.host.request.ModifyHostInfoRequest;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.dto.host.response.GetHostInfoResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.wallet.WalletService;
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
    private final WalletService walletService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    // 사업자 등록번호 확인 정규식
    private static final String REGEX = "^\\d{3}-\\d{2}-\\d{5}$";

    // 주최측 회원가입
    @Override
    @Transactional
    public void hostSignup(HostSignupRequest request) throws Exception {
        // 1단계: 사업자등록번호 형식 검증
        if (!request.businessNo().matches(REGEX)) {
            throw new BadRequestException("잘못된 사업자 등록번호 형식입니다.");
        }
        // 2단계: 이메일 중복 체크
        if (hostRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 존재하는 이메일 입니다.");
        }
        // 3단계: 사업자등록번호 중복 체크
        if (hostRepository.existsByBusinessNo(request.businessNo())) {
            throw new ConflictException("이미 존재하는 사업자 등록번호입니다.");
        }

        // 4단계: 지갑 생성
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

        // 5단계: 플랫폼 지갑 → 신규 유저 지갑으로 초기 SSF 전송 (비동기)
        walletService.transferInitialFunds(address);
    }

    // 사업자 등록번호 중복 확인
    @Override
    public CheckBusinessNoDuplicateResponse checkBusinessNoDuplicate(String businessNo) {
        if (!businessNo.matches(REGEX)) {
            throw new BadRequestException("잘못된 사업자 등록번호 형식입니다.");
        }

        boolean status = hostRepository.existsByBusinessNo(businessNo);
        return new CheckBusinessNoDuplicateResponse(status);
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
