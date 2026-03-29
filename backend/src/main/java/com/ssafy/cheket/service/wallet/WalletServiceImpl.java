package com.ssafy.cheket.service.wallet;

import com.ssafy.cheket.dto.wallet.response.WalletBalanceResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;

import java.io.File;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지갑 서비스 구현체 — 초기 SSF 전송
 *
 * [이 클래스의 역할 — 한 줄 요약] 회원가입 시 플랫폼 지갑에서 신규 유저/호스트 지갑으로 초기 SSF를 보내주는 역할.
 *
 * [이전 방식 vs 현재 방식] 이전: tx 전송 후 1초×10회 폴링으로 확인 → SSAFY 블록 10초라 타임아웃 발생 현재: tx
 * 전송만 하고 바로 종료 → 이벤트 리스너(SsfTransferEventListener)가 나중에 블록 확정을 감지하면 Transaction
 * CONFIRMED + 잔액 업데이트
 *
 * [흐름] 1. (유저만) Transaction 생성 — PENDING 상태, txHash 없음 2. 블록체인에 ERC-20
 * transfer() 함수 호출 3. (유저만) Transaction 업데이트 — SUBMITTED 상태, txHash 저장 4. 메서드
 * 종료 ← 여기서 끝! 빠르게 리턴. 5. (5초 후) 이벤트 리스너가 Transfer 이벤트 감지 → Transaction
 * CONFIRMED + 지갑 잔액 업데이트
 */
@Slf4j // Lombok이 제공하는 로깅 어노테이션
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final Web3j web3j; // 블록체인 RPC 클라이언트
    private final TransactionService transactionService; // 트랜잭션 상태 관리
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final HostRepository hostRepository;

    // 플랫폼 지갑 개인키 (application.yml에서 주입)
    // 이 키로 서명해서 플랫폼 지갑에서 SSF를 보냄
    @Value("${blockchain.platform-private-key}")
    private String platformPrivateKey;

    // 초기 전송 SSF 수량 (application.yml에서 주입)
    @Value("${blockchain.initial-transfer-amount}")
    private long initialTransferAmount;

    // SSF 토큰 컨트랙트 주소 (application.yml에서 주입)
    @Value("${blockchain.ssf-contract-address}")
    private String ssfContractAddress;

    // SSAFY 네트워크 체인 ID (application.yml에서 주입)
    @Value("${blockchain.chain-id}")
    private long chainId;

    /**
     * 비동기 초기 SSF 전송
     *
     * [호출 시점] - UserServiceImpl.userSignup() → userId = user.getId() (유저) -
     * HostServiceImpl.hostSignup() → userId = null (호스트)
     *
     * [@Async 의미] 이 메서드는 별도 스레드에서 실행된다. → 회원가입 API는 블록체인 응답을 기다리지 않고 바로 HTTP 200 리턴
     * → 사용자 입장에서 가입이 빠르게 느껴짐
     *
     * [userId가 null인 경우 (호스트)] 호스트는 Transaction 레코드를 만들지 않는다. - buyer_id가 users(id)
     * FK인데 호스트 ID를 넣으면 FK 위반 - 호스트는 거래 주체가 아니라 공연 주최자 - 블록체인에 기록은 남고, 이벤트 리스너가 잔액
     * 업데이트는 해줌
     */
    @Async
    @Override
    public void transferInitialFunds(String toAddress, Long userId) {

        // ── 1단계: (유저만) Transaction 생성 — PENDING 상태 ──
        // 블록체인에 보내기 "전에" DB에 기록을 먼저 남긴다.
        // → tx 전송 중 서버가 죽어도 PENDING 레코드로 "미완료 전송" 추적 가능
        Transaction pendingTx = null;
        if (userId != null) {
            pendingTx = transactionService.createPendingTransaction(Transaction.TransactionType.TRANSFER,
                initialTransferAmount, "초기 SSF 전송: platform → " + toAddress, userId, null // sellerId = null (플랫폼)
            );

            log.info("Transaction 생성 (PENDING): id={}, to={}", pendingTx.getId(), toAddress);
        } else {
            log.info("호스트 초기 SSF 전송 — Transaction 기록 생략: to={}", toAddress);
        }

        try {
            // ── 2단계: 플랫폼 지갑 준비 ──
            // 개인키로 Credentials 생성 → 이걸로 트랜잭션에 서명
            Credentials platformCredentials = Credentials.create(platformPrivateKey);
            String platformAddress = platformCredentials.getAddress();
            log.info("플랫폼 지갑: {}, 전송량: {} SSF", platformAddress, initialTransferAmount);

            // ── 3단계: SSF 토큰 decimals 조회 ──
            // ERC-20 토큰마다 decimals가 다름 (USDT=6, ETH=18)
            // SSAFY SSF는 decimals=0 → 1 SSF = 실제 1단위
            // 하지만 하드코딩하지 않고 컨트랙트에서 직접 읽어옴 (범용성)
            Function decimalsFunction = new Function("decimals", // 호출할 함수명
                Collections.emptyList(), // 입력 파라미터 없음
                Collections.singletonList(new TypeReference<Uint256>() {
                }) // 반환 타입: uint256
            );
            String decimalsEncoded = FunctionEncoder.encode(decimalsFunction);

            // eth_call = 블록체인 상태 조회 (가스 안 씀, 상태 변경 없음)
            String decimalsResult = web3j
                .ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(platformAddress,
                    ssfContractAddress, decimalsEncoded), DefaultBlockParameterName.LATEST)
                .send().getValue();

            int decimals = new BigInteger(decimalsResult.substring(2), 16).intValue();

            // decimals 적용: 실제 전송량 = amount × 10^decimals
            // SSF는 decimals=0이므로 tokenAmount = initialTransferAmount 그대로
            BigInteger tokenAmount = BigInteger.valueOf(initialTransferAmount).multiply(BigInteger.TEN.pow(decimals));

            // ── 4단계: ERC-20 transfer() 함수 인코딩 ──
            // transfer(address to, uint256 amount) — ERC-20 표준 전송 함수
            // 이걸 ABI 인코딩해서 트랜잭션 data 필드에 넣음
            Function function = new Function("transfer", Arrays.asList(new Address(toAddress), // 수신 주소
                new Uint256(tokenAmount) // 전송량
            ), Collections.singletonList(new TypeReference<Bool>() {
            }) // 반환 타입: bool
            );
            String encodedFunction = FunctionEncoder.encode(function);

            // ── 5단계: 블록체인에 트랜잭션 전송 ──
            // RawTransactionManager: 개인키로 직접 서명해서 전송 (키스토어 파일 불필요)
            // gasPrice = 0: SSAFY 네트워크는 가스비 무료
            // gasLimit = 100000: ERC-20 transfer는 보통 6만 가스 정도 → 넉넉히 10만
            // value = 0: SSF(ERC-20) 전송이지 ETH 전송이 아니므로 ETH value = 0
            RawTransactionManager txManager = new RawTransactionManager(web3j, platformCredentials, chainId);

            EthSendTransaction tx = txManager.sendTransaction(BigInteger.ZERO, // gasPrice = 0 (SSAFY 무료 가스)
                BigInteger.valueOf(100000), // gasLimit = 100,000
                ssfContractAddress, // to = SSF 컨트랙트 주소 (transfer 함수 호출)
                encodedFunction, // data = transfer(toAddress, amount) 인코딩
                BigInteger.ZERO // value = 0 ETH (ERC-20 전송이므로)
            );

            // ── 6단계: RPC 응답 에러 체크 ──
            // tx를 블록체인 노드에 보냈지만, 노드가 거부할 수 있음
            // (잔액 부족, nonce 충돌, 가스 한도 초과 등)
            if (tx.hasError()) {
                log.error("SSF 전송 RPC 에러: {} - {}", toAddress, tx.getError().getMessage());
                // 유저인 경우에만 FAILED로 업데이트
                if (pendingTx != null) {
                    transactionService.updateToFailed(pendingTx.getId(), "RPC: " + tx.getError().getMessage());
                }
                return;
            }

            // ── 7단계: (유저만) Transaction → SUBMITTED 업데이트 ──
            // 블록체인 노드가 tx를 수락함 → txHash를 받았음
            // 이제 이 tx는 멤풀(대기열)에 있고, 블록에 포함되길 기다리는 중
            // → 이벤트 리스너가 나중에 이 txHash를 찾아서 CONFIRMED로 바꿔줌
            String txHash = tx.getTransactionHash();
            if (pendingTx != null) {
                transactionService.updateToSubmitted(pendingTx.getId(), txHash);
                log.info("Transaction 제출 (SUBMITTED): id={}, txHash={}", pendingTx.getId(), txHash);
            } else {
                log.info("호스트 SSF 전송 완료 (Transaction 없음): txHash={}", txHash);
            }

            // ========================================
            // 여기서 끝! 이벤트 리스너가 나머지를 처리:
            // SsfTransferEventListener가 5초 후 Transfer 이벤트 감지
            // → Transaction CONFIRMED 업데이트 + 지갑 잔액 반영
            // ========================================

        } catch (Exception e) {
            log.error("SSF 전송 실패: {}", toAddress, e);
            // 유저인 경우에만 FAILED로 업데이트
            if (pendingTx != null) {
                transactionService.updateToFailed(pendingTx.getId(), "Exception: " + e.getMessage());
            }
        }
    }

    // -- 기본 잔액 조회 (DB) --
    // 이벤트 리스너가 이미 ctkBalance를 동기화하고 있으므로
    // DB에서 읽어도 충분히 정확함
    @Override
    public WalletBalanceResponse getBalance(Long id, String role) {
        Wallet wallet;

        // 1. Host/User 판별
        if ("ROLE_HOST".equals(role)) {
            Host host = hostRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("호스트가 존재하지 않습니다."));
            wallet = walletRepository.findById(host.getWalletId())
                .orElseThrow(() -> new NotFoundException("지갑이 존재하지 않습니다."));
        } else {
            User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));
            wallet = walletRepository.findById(user.getWalletId())
                .orElseThrow(() -> new NotFoundException("지갑이 존재하지 않습니다."));
        }

        // 2. 지갑에서 잔액 읽기
        Integer balance = wallet.getCtkBalance() != null ? wallet.getCtkBalance() : 0;
        return new WalletBalanceResponse(balance, wallet.getAddress());
    }

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    // -- 테스트 계정(userId 27~141) SSF 일괄 회수 → 플랫폼 지갑으로 --
    @Override
    public Map<String, Object> collectSsfFromTestAccounts() {
        Credentials platformCredentials = Credentials.create(platformPrivateKey);
        String platformAddress = platformCredentials.getAddress();

        int success = 0;
        int skipped = 0;
        int failed = 0;
        long totalCollected = 0;
        List<Map<String, Object>> results = new ArrayList<>();

        for (long userId = 27; userId <= 141; userId++) {
            try {
                User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
                if (user == null) {
                    skipped++;
                    continue;
                }
                Wallet wallet = walletRepository.findById(user.getWalletId()).orElse(null);
                if (wallet == null || wallet.getKeystoreFilename() == null) {
                    skipped++;
                    continue;
                }

                // ① 온체인 잔액 조회
                Function balanceOfFunction = new Function("balanceOf",
                    Collections.singletonList(new Address(wallet.getAddress())),
                    Collections.singletonList(new TypeReference<Uint256>() {
                    }));
                String encodedBalance = FunctionEncoder.encode(balanceOfFunction);
                EthCall ethCall = web3j
                    .ethCall(
                        org.web3j.protocol.core.methods.request.Transaction
                            .createEthCallTransaction(wallet.getAddress(), ssfContractAddress, encodedBalance),
                        DefaultBlockParameterName.LATEST)
                    .send();

                if (ethCall.hasError()) {
                    failed++;
                    continue;
                }

                List<Type> decoded = FunctionReturnDecoder.decode(ethCall.getValue(),
                    balanceOfFunction.getOutputParameters());
                BigInteger balance = (BigInteger) decoded.get(0).getValue();

                if (balance.compareTo(BigInteger.ZERO) <= 0) {
                    skipped++;
                    continue;
                }

                // ② 유저 키스토어로 서명하여 transfer(platformAddress, balance)
                Credentials userCredentials = WalletUtils.loadCredentials(keystorePassword,
                    new File(keystoreDirectory + "/" + wallet.getKeystoreFilename()));
                RawTransactionManager txManager = new RawTransactionManager(web3j, userCredentials, chainId);

                Function transferFunction = new Function("transfer",
                    Arrays.asList(new Address(platformAddress), new Uint256(balance)),
                    Collections.singletonList(new TypeReference<Bool>() {
                    }));
                String encodedTransfer = FunctionEncoder.encode(transferFunction);

                EthSendTransaction tx = txManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                    ssfContractAddress, encodedTransfer, BigInteger.ZERO);

                if (tx.hasError()) {
                    log.warn("[SSF 회수] userId={} 전송 실패: {}", userId, tx.getError().getMessage());
                    failed++;
                    continue;
                }

                // ③ DB 잔액 0으로 업데이트
                wallet.setCtkBalance(0);
                wallet.setBalanceSyncedAt(LocalDateTime.now());
                walletRepository.save(wallet);

                totalCollected += balance.longValue();
                success++;

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("userId", userId);
                result.put("username", user.getUsername());
                result.put("amount", balance.longValue());
                result.put("txHash", tx.getTransactionHash());
                results.add(result);

                log.info("[SSF 회수] userId={}, username={}, amount={}, txHash={}", userId, user.getUsername(),
                    balance, tx.getTransactionHash());

            } catch (Exception e) {
                log.error("[SSF 회수] userId={} 실패", userId, e);
                failed++;
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalCollected", totalCollected);
        response.put("success", success);
        response.put("skipped", skipped);
        response.put("failed", failed);
        response.put("platformAddress", platformAddress);
        response.put("details", results);

        log.info("[SSF 회수] 완료 — 총 {}SSF 회수, 성공={}, 스킵={}, 실패={}", totalCollected, success, skipped, failed);
        return response;
    }

    // -- 새로고침 잔액 조회 (온체인 -> DB 동기화) --
    // 블록체인 노드에 직접 balanceOf() 호출 -> DB ctkBalance 업데이트
    @Override
    public WalletBalanceResponse refreshBalance(Long id, String role) {
        Wallet wallet;

        if ("ROLE_HOST".equals(role)) {
            Host host = hostRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("호스트가 존재하지 않습니다."));
            wallet = walletRepository.findById(host.getWalletId())
                .orElseThrow(() -> new NotFoundException("지갑이 존재하지 않습니다."));
        } else {
            User user = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("유저가 존재하지 않습니다."));
            wallet = walletRepository.findById(user.getWalletId())
                .orElseThrow(() -> new NotFoundException("지갑이 존재하지 않습니다."));
        }

        try {
            // 1. ERC-20 balanceOf(address) 온체인 호출 (스마트 컨트랙트의 balanceOf(address) 함수를 Java
            // 객체로 정의)
            Function balanceOfFuntion = new Function("balanceOf",
                Collections.singletonList(new Address(wallet.getAddress())), // 입력: 누구 잔액?
                Collections.singletonList(new TypeReference<Uint256>() { // 출력: 숫자(잔액)
                }));
            // ABI 인코딩
            String encodedFunction = FunctionEncoder.encode(balanceOfFuntion);

            // ethCal은 조회 전용 - 트랜잭션 발생 안 하고, 가스비 없음
            EthCall ethCall = web3j
                .ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    wallet.getAddress(), ssfContractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                .send();

            // 노드가 다운되었거나, 컨트랙트 주소가 잘못된 경우 등을 처리
            if (ethCall.hasError()) {
                throw new BlockchainException("블록체인 노드 통신 오류");
            }

            // 2. 결과 디코딩
            List<Type> decoded = FunctionReturnDecoder.decode(ethCall.getValue(),
                balanceOfFuntion.getOutputParameters());
            BigInteger rawBalance = (BigInteger) decoded.get(0).getValue();
            int onChainBalance = rawBalance.intValue();

            // 3. DB 동기화 - 온체인 잔액으로 덮어쓰기
            wallet.setCtkBalance(onChainBalance);
            wallet.setBalanceSyncedAt(LocalDateTime.now());
            walletRepository.save(wallet);

            log.info("온체인 잔액 동기화: {} -> {} CTK", wallet.getAddress(), onChainBalance);

            return new WalletBalanceResponse(onChainBalance, wallet.getAddress());
        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            throw new BlockchainException("블록체인 노드 통신 오류");
        }

    }

}
