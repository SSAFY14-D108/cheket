package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.enums.ResaleStatus;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * TicketPurchaseAsyncWorker — 티켓 구매 블록체인 처리 (비동기)
 *
 * [왜 별도 클래스?] Spring @Async는 프록시 기반이라 같은 클래스 내부 호출 시 무시됨
 * TicketPurchaseService에서 이 클래스의 메서드를 호출해야 @Async가 동작
 *
 * [역할] ① 사용자 지갑 Keystore 로드 (Custodial 대리 서명) ② 온체인에서 티켓 가격 조회 ③ SSF.approve()
 * — 사용자 키로 대리 서명 ④ PurchaseRouter.purchaseTicket() × 좌석 수 — 플랫폼 키로 서명 ⑤ 성공: 좌석
 * SOLD + Ticket 생성 + Transaction CONFIRMED ⑥ 실패: 좌석 AVAILABLE 복원 + Transaction
 * FAILED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketPurchaseAsyncWorker {

    private final BlockchainService blockchainService;
    private final SessionSeatRepository sessionSeatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    /**
     * 블록체인 구매 처리 — @Async로 별도 스레드에서 실행
     *
     * TicketPurchaseService.purchaseTickets()가 호출 → 별도 클래스이므로 Spring 프록시가 @Async를
     * 인식 → 새 스레드에서 실행, 호출한 쪽은 즉시 반환
     */
    @Async
    @Transactional
    public void processOnChainPurchase(Long txId, Long userId, Long showId, Long sessionId, List<Long> sessionSeatIds,
        Long onChainSessionIdValue) {
        log.info("[티켓 구매 비동기] 시작 — txId={}", txId);

        // 좌석 다시 조회 (별도 트랜잭션이므로)
        List<SessionSeat> seats = sessionSeatRepository.findAllById(sessionSeatIds);

        try {
            // ========== ① 사용자 지갑 로드 ==========
            // Custodial: 서버가 사용자의 Keystore 파일을 열어서 대리 서명
            User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BlockchainException("사용자를 찾을 수 없습니다: " + userId));

            Wallet wallet = walletRepository.findById(user.getWalletId())
                .orElseThrow(() -> new BlockchainException("지갑을 찾을 수 없습니다"));

            Credentials buyerCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + wallet.getKeystoreFilename()));
            String buyerAddress = wallet.getAddress();

            // ========== ② 온체인에서 총 가격 조회 ==========
            // DB가 아닌 온체인이 진실 → 가격 조작 불가
            BigInteger totalPrice = BigInteger.ZERO;
            for (SessionSeat seat : seats) {
                BigInteger price = blockchainService.getTicketNFT()
                    .getPrice(BigInteger.valueOf(seat.getOnChainTicketNftId())).send();
                totalPrice = totalPrice.add(price);
            }
            log.info("[티켓 구매 비동기] 총 가격: {} SSF", totalPrice);

            // Transaction 금액 + 상태 업데이트
            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setAmount(totalPrice.longValue());
            tx.setDescription("SSF 승인 중");
            transactionRepository.save(tx);

            // ========== ③ SSF.approve() — 사용자 키로 대리 서명 ==========
            // "PurchaseRouter가 내 SSF를 totalPrice만큼 써도 돼"
            // 사용자의 개인키로 서명 (Custodial 대리 서명)
            RawTransactionManager buyerTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                buyerCredentials, blockchainService.getChainId());

            Function approveFunction = new Function("approve",
                Arrays.asList(new Address(blockchainService.getPurchaseRouter().getContractAddress()),
                    new Uint256(totalPrice)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
            String encodedApprove = FunctionEncoder.encode(approveFunction);

            EthSendTransaction approveTx = buyerTxManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                blockchainService.getSsfContractAddress(), encodedApprove, BigInteger.ZERO);

            if (approveTx.hasError()) {
                throw new BlockchainException("SSF approve 실패: " + approveTx.getError().getMessage());
            }

            log.info("[티켓 구매 비동기] approve 전송 완료 — txHash={}", approveTx.getTransactionHash());

            // approve TX 블록 확정 대기
            waitForTransaction(approveTx.getTransactionHash());

            // Transaction 상태 업데이트: approve 확정
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            tx.setDescription("SSF 승인 완료, 티켓 구매 진행 중");
            transactionRepository.save(tx);

            log.info("[티켓 구매 비동기] approve 블록 확정 완료");

            // ========== ④ 좌석별 PurchaseRouter.purchaseTicket() ==========
            // 플랫폼 키로 서명 (onlyOwner)
            // 컨트랙트 내부에서 원자적으로:
            // SSF: buyer → Settlement (자금 잠금)
            // NFT: platform → buyer (소유권 이전)
            // walletTicketCount 업데이트
            BigInteger onChainSessionId = BigInteger.valueOf(onChainSessionIdValue);
            String lastTxHash = null;

            for (int i = 0; i < seats.size(); i++) {
                SessionSeat seat = seats.get(i);
                BigInteger ticketNftId = BigInteger.valueOf(seat.getOnChainTicketNftId());

                // 진행 상태 업데이트
                tx.setDescription("티켓 구매 중 (" + (i + 1) + "/" + seats.size() + ")");
                transactionRepository.save(tx);

                TransactionReceipt receipt = blockchainService.getPurchaseRouter()
                    .purchaseTicket(buyerAddress, ticketNftId, onChainSessionId).send();

                lastTxHash = receipt.getTransactionHash();
                log.info("[티켓 구매 비동기] purchaseTicket 완료 — ticketNftId={}, txHash={}", ticketNftId, lastTxHash);

                // SessionSeat → SOLD
                seat.setStatus(SeatStatus.SOLD);
                sessionSeatRepository.save(seat);

                // Ticket 레코드 생성
                Ticket ticket = Ticket.builder().userId(userId).sessionSeatId(seat.getId())
                    .numbering(generateTicketNumber()).ticketNftId(seat.getOnChainTicketNftId())
                    .resaleStatus(ResaleStatus.AVAILABLE).build();
                ticketRepository.save(ticket);
            }

            // ========== ⑤ Transaction → CONFIRMED ==========
            tx.setTxHash(lastTxHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription("구매 완료 (" + seats.size() + "매, " + totalPrice + " SSF)");
            transactionRepository.save(tx);

            log.info("[티켓 구매 비동기] 완료 — txId={}, {}매, {} SSF", txId, seats.size(), totalPrice);

        } catch (Exception e) {
            // ========== ⑥ 실패: 좌석 복원 + Transaction FAILED ==========
            log.error("[티켓 구매 비동기] 실패 — txId={}", txId, e);

            // 좌석 PENDING_TX → AVAILABLE 복원
            for (SessionSeat seat : seats) {
                if (seat.getStatus() == SeatStatus.PENDING_TX) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                }
            }
            sessionSeatRepository.saveAll(seats);

            // Transaction → FAILED
            try {
                Transaction tx = transactionRepository.findById(txId).orElseThrow();
                tx.setTxStatus(Transaction.TxStatus.FAILED);
                tx.setDescription("구매 실패: " + e.getMessage());
                transactionRepository.save(tx);
            } catch (Exception dbErr) {
                log.error("[티켓 구매 비동기] Transaction FAILED 업데이트 실패", dbErr);
            }
        }
    }

    /**
     * TX가 블록에 포함될 때까지 대기 (최대 30초)
     */
    private void waitForTransaction(String txHash) throws Exception {
        int maxAttempts = 30;
        for (int i = 0; i < maxAttempts; i++) {
            var receipt = blockchainService.getWeb3j().ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            if (receipt.isPresent()) {
                return;
            }
            Thread.sleep(1000);
        }
        throw new BlockchainException("TX 확정 대기 타임아웃: " + txHash);
    }

    /**
     * 티켓 번호 생성 (고유 식별자)
     */
    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
