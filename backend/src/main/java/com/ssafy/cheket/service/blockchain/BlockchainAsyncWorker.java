package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.entity.ticket.TicketTransfer;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.entity.wallet.Wallet;
import com.ssafy.cheket.enums.ResaleStatus;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.blockchain.contract.StakeholderNFT.StakeholderMintedEventResponse;
import com.ssafy.cheket.entity.resale.Resale;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.entity.show.Seat;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SeatRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.ticket.TicketTransferRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.repository.resale.ResaleEntityRepository;
import com.ssafy.cheket.repository.wallet.WalletRepository;
import com.ssafy.cheket.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * BlockchainAsyncWorker — 티켓 관련 블록체인 비동기 처리 통합 클래스
 *
 * [왜 별도 클래스?] Spring @Async는 프록시 기반이라 같은 클래스 내부 호출 시 무시됨 Service에서 이 클래스의 메서드를
 * 호출해야 @Async가 동작
 *
 * [역할] ① processOnChainPurchase() — 1차 티켓 구매 (approve + purchaseTicket) ②
 * processOnChainTransfer() — 지정 양도 (directTransfer) ③ processOnChainRefund() —
 * 환불 ④ processOnChainResale() — 리세일
 *
 * [@Transactional 없는 이유] save() 호출 시 즉시 DB 커밋 → catch에서 FAILED 업데이트가 롤백되지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainAsyncWorker {

    private final BlockchainService blockchainService;
    private final ResaleEntityRepository resaleEntityRepository;
    private final StakeholderRepository stakeholderRepository;
    private final HostRepository hostRepository;
    private final SeatRepository seatRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ShowRepository showRepository;
    private final SessionRepository sessionRepository;
    private final TicketTransferRepository ticketTransferRepository;
    private final NotificationService notificationService;

    @Value("${wallet.keystore.password}")
    private String keystorePassword;

    @Value("${wallet.keystore.directory}")
    private String keystoreDirectory;

    // ==================== 1차 구매 ====================

    /**
     * 블록체인 구매 처리 — @Async로 별도 스레드에서 실행
     *
     * [흐름] ① 사용자 지갑 Keystore 로드 (Custodial 대리 서명) ② 온체인에서 티켓 가격 조회 ③ SSF.approve()
     * — 사용자 키로 대리 서명 ④ PurchaseRouter.purchaseTicket() × 좌석 수 — 플랫폼 키로 서명 ⑤ 성공: 좌석
     * SOLD + Ticket 생성 + Transaction CONFIRMED ⑥ 실패: 좌석 AVAILABLE 복원 + Transaction
     * FAILED
     */
    @Async
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

            log.info("[티켓 구매 비동기] 사용자 지갑 로드 — userId={}, walletId={}", userId, user.getWalletId());
            // keystore 파일 열어서, 비밀번호로 복호화, 사용자의 개인키 꺼내서 approve TX에 서명 (Custodial 대리 서명)
            Credentials buyerCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + wallet.getKeystoreFilename()));

            String buyerAddress = wallet.getAddress();
            log.info("[티켓 구매 비동기] 사용자 지갑 주소: {}", buyerAddress);

            // ========== ② 온체인에서 총 가격 조회 ==========
            // DB가 아닌 온체인이 진실 → 가격 조작 불가
            BigInteger totalPrice = BigInteger.ZERO;
            for (SessionSeat seat : seats) {
                BigInteger price = blockchainService.getTicketNFT()
                    .getPrice(BigInteger.valueOf(seat.getOnChainTicketNftId())).send();
                totalPrice = totalPrice.add(price);
                log.info("[티켓 구매 비동기] 티켓 가격 조회 — nftId={}, price={} SSF", seat.getOnChainTicketNftId(), price);
            }
            log.info("[티켓 구매 비동기] 총 가격: {} SSF", totalPrice);

            // Transaction 금액 + 상태 업데이트
            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setAmount(totalPrice.longValue());
            tx.setDescription("전자서명 처리 중 — SSF 결제 승인 (buyerUserId=%d, seats=%d, totalPrice=%d SSF)".formatted(userId,
                seats.size(), totalPrice.longValue()));
            transactionRepository.save(tx);
            log.info("[티켓 구매 비동기] Transaction 업데이트 — totalPrice={}", totalPrice);

            // ========== ③ SSF.approve() — 사용자 키로 대리 서명 ==========
            // "PurchaseRouter가 내 SSF 400개를 써도 돼" 허락
            RawTransactionManager buyerTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                buyerCredentials, blockchainService.getChainId());

            log.info("[티켓 구매 비동기] SSF approve 준비 — PurchaseRouter={}, totalPrice={}",
                blockchainService.getPurchaseRouter().getContractAddress(), totalPrice);
            Function approveFunction = new Function("approve",
                Arrays.asList(new Address(blockchainService.getPurchaseRouter().getContractAddress()), // 누구에게 허락:
                                                                                                       // PurchaseRouter
                    new Uint256(totalPrice)), // 얼마까지: 400 SSF
                Collections.singletonList(new TypeReference<Bool>() {
                }));
            String encodedApprove = FunctionEncoder.encode(approveFunction);

            log.info("[티켓 구매 비동기] SSF approve TX 전송 중...");
            EthSendTransaction approveTx = buyerTxManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                blockchainService.getSsfContractAddress(), encodedApprove, BigInteger.ZERO);
            // → txHash = "0xapprove123..." 받음

            if (approveTx.hasError()) {
                throw new BlockchainException("SSF approve 실패: " + approveTx.getError().getMessage());
            }
            log.info("[티켓 구매 비동기] approve TX 전송 완료 — txHash={}", approveTx.getTransactionHash());

            // approve TX 블록 확정 대기
            waitForTransaction(approveTx.getTransactionHash());
            log.info("[티켓 구매 비동기] approve 블록 확정 완료");

            // Transaction 상태 업데이트
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            tx.setDescription("전파 완료 — SSF approve 확정, NFT 이전 준비 (approveTxHash=%s, onChainSessionId=%d)"
                .formatted(approveTx.getTransactionHash(), onChainSessionIdValue));
            transactionRepository.save(tx);
            log.info("[티켓 구매 비동기] Transaction → SUBMITTED");

            // ========== ④ 좌석별 PurchaseRouter.purchaseTicket() (재시도 + 전체 취소) ==========
            // 플랫폼 키로 서명 — 컨트랙트 내부에서 원자적 처리:
            // SSF: buyer → Settlement (자금 잠금) + NFT: platform → buyer (소유권 이전)
            BigInteger onChainSessionId = BigInteger.valueOf(onChainSessionIdValue);
            String lastTxHash = null;

            // 성공한 좌석 추적 (실패 시 전체 환불용)
            List<PurchasedSeatInfo> purchasedSeats = new ArrayList<>();

            for (int i = 0; i < seats.size(); i++) {
                SessionSeat seat = seats.get(i);
                BigInteger ticketNftId = BigInteger.valueOf(seat.getOnChainTicketNftId());

                tx.setDescription("블록 생성 대기 중 — 티켓 NFT 소유권 이전 (%d/%d, sessionSeatId=%d, ticketNftId=%d)"
                    .formatted(i + 1, seats.size(), seat.getId(), seat.getOnChainTicketNftId()));
                transactionRepository.save(tx);
                log.info("[티켓 구매 비동기] purchaseTicket 호출 — {}/{}, nftId={}", i + 1, seats.size(), ticketNftId);

                // 3회 재시도 (네트워크 일시 장애 대비)
                int maxRetry = 3;
                boolean success = false;
                TransactionReceipt receipt = null;

                for (int attempt = 1; attempt <= maxRetry; attempt++) {
                    try {
                        receipt = blockchainService.getPurchaseRouter()
                            .purchaseTicket(buyerAddress, ticketNftId, onChainSessionId).send();
                        success = true;
                        break;
                    } catch (Exception retryErr) {
                        if (attempt < maxRetry) {
                            log.warn("[티켓 구매 비동기] purchaseTicket 실패 {}/{}회, 2초 후 재시도 — nftId={}", attempt, maxRetry,
                                ticketNftId);
                            Thread.sleep(2000);
                        } else {
                            log.error("[티켓 구매 비동기] purchaseTicket {}회 모두 실패 — nftId={}", maxRetry, ticketNftId);
                        }
                    }
                }

                if (!success) {
                    // 3회 다 실패 → 성공한 것만 유지 + 관리자 알림
                    // 성공한 좌석 정보
                    String successInfo = purchasedSeats.stream().map(info -> {
                        Seat seatInfo = seatRepository.findById(info.seat.getSeatId()).orElse(null);
                        String seatNo = seatInfo != null ? seatInfo.getSeatNo() : "?";
                        return seatNo + "(id:" + info.seat.getId() + ")";
                    }).collect(java.util.stream.Collectors.joining(", "));
                    // 실패한 좌석 정보
                    StringBuilder failedInfo = new StringBuilder();
                    for (int j = i; j < seats.size(); j++) {
                        SessionSeat failedSeat = seats.get(j);
                        Seat seatInfo = seatRepository.findById(failedSeat.getSeatId()).orElse(null);
                        String seatNo = seatInfo != null ? seatInfo.getSeatNo() : "?";
                        if (!failedInfo.isEmpty()) {
                            failedInfo.append(", ");
                        }
                        failedInfo.append(seatNo).append("(id:").append(failedSeat.getId()).append(")");
                    }

                    log.error("[티켓 구매 비동기] 부분 실패 — 성공: [{}], 실패: [{}], 관리자 확인 필요", successInfo, failedInfo);
                    throw new BlockchainException(
                        "일부 좌석 구매 실패 (성공: [" + successInfo + "], 실패: [" + failedInfo + "]) — 관리자에게 문의해주세요.");
                }

                lastTxHash = receipt.getTransactionHash();
                log.info("[티켓 구매 비동기] purchaseTicket 완료 — nftId={}, txHash={}", ticketNftId, lastTxHash);

                // SessionSeat → SOLD
                seat.setStatus(SeatStatus.SOLD);
                sessionSeatRepository.save(seat);

                // Ticket 레코드 생성
                Ticket ticket = Ticket.builder().userId(userId).sessionSeatId(seat.getId())
                    .numbering(generateTicketNumber()).ticketNftId(seat.getOnChainTicketNftId())
                    .resaleStatus(ResaleStatus.AVAILABLE).build();
                ticketRepository.save(ticket);
                log.info("[티켓 구매 비동기] DB 업데이트 — seatId={} → SOLD, ticket 생성", seat.getId());

                // 성공 목록에 추가 (실패 시 환불용)
                purchasedSeats.add(new PurchasedSeatInfo(seat, ticket, ticketNftId));
            }

            // ========== ⑤ Transaction → CONFIRMED ==========
            tx.setTxHash(lastTxHash); // purchase tx로 수정
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription("블록체인 확정 — 구매 완료 (buyerUserId=%d, seats=%d, amount=%d SSF, txHash=%s)".formatted(userId,
                seats.size(), totalPrice.longValue(), lastTxHash));
            transactionRepository.save(tx);
            log.info("[티켓 구매 비동기] 완료 — txId={}, {}매, {} SSF", txId, seats.size(), totalPrice);

        } catch (BlockchainException e) {
            log.error("[티켓 구매 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[티켓 구매 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, "티켓 구매 실패 — " + e.getMessage());
        }
    }

    // ==================== 지정 양도 ====================

    /**
     * 블록체인 양도 처리 — @Async로 별도 스레드에서 실행
     *
     * [구매와의 차이] 구매: approve TX (사용자 키) → purchaseTicket TX (플랫폼 키) = 2 TX, ~30초 양도:
     * directTransfer TX (플랫폼 키) = 1 TX, ~10초 SSF 이동 없음 → approve 불필요
     *
     * [Marketplace.directTransfer() 컨트랙트 내부 동작] ① ticketNFT.getStatus(ticketId) ==
     * VALID 검증 ② eventNFT.decrementWalletTicketCount(eventId, from) → 보내는 사람 -1 ③
     * eventNFT.incrementWalletTicketCount(eventId, to) → 받는 사람 +1 (maxPerWallet 초과
     * 시 revert) ④ ticketNFT.transferFrom(from, to, ticketId) → NFT 소유권 이전 → 하나라도
     * 실패하면 전부 revert (원자적)
     */
    @Async
    public void processOnChainTransfer(Long txId, Long senderUserId, Long receiverUserId, Long ticketId,
        Long onChainTicketNftId) {
        log.info("[양도 비동기] 시작 — txId={}, ticketId={}, ticketNftId={}", txId, ticketId, onChainTicketNftId);

        try {
            // ========== ① 보내는 사람 + 받는 사람 지갑 주소 조회 ==========
            User sender = userRepository.findByIdAndDeletedAtIsNull(senderUserId)
                .orElseThrow(() -> new BlockchainException("보내는 사람을 찾을 수 없습니다."));
            Wallet senderWallet = walletRepository.findById(sender.getWalletId())
                .orElseThrow(() -> new BlockchainException("보내는 사람 지갑을 찾을 수 없습니다."));
            String senderAddress = senderWallet.getAddress();
            log.info("[양도 비동기] 보내는 사람 지갑 조회 — userId={}, address={}", senderUserId, senderAddress);

            User receiver = userRepository.findByIdAndDeletedAtIsNull(receiverUserId)
                .orElseThrow(() -> new BlockchainException("받는 사람을 찾을 수 없습니다."));
            Wallet receiverWallet = walletRepository.findById(receiver.getWalletId())
                .orElseThrow(() -> new BlockchainException("받는 사람 지갑을 찾을 수 없습니다."));
            String receiverAddress = receiverWallet.getAddress();
            log.info("[양도 비동기] 받는 사람 지갑 조회 — userId={}, address={}", receiverUserId, receiverAddress);

            // ========== ② 보내는 사람 NFT approve ==========
            // 구매자(보내는 사람)가 소유한 NFT를 Marketplace가 대신 옮길 수 있도록 허락
            // 구매 때 SSF.approve()를 구매자 키로 서명한 것과 동일한 패턴
            //
            // [구매 vs 양도 차이]
            // 구매: SSF.approve(PurchaseRouter, 금액) → ERC-20 돈 허락
            // → 플랫폼 소유 NFT를 구매자에게 → 플랫폼 approval로 충분
            // 양도: TicketNFT.setApprovalForAll(Marketplace, true) → ERC-721 NFT 허락
            // → 구매자 소유 NFT를 받는사람에게 → 구매자 approval 필요
            Credentials senderCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + senderWallet.getKeystoreFilename()));

            RawTransactionManager senderTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                senderCredentials, blockchainService.getChainId());

            // setApprovalForAll(Marketplace주소, true) 인코딩
            Function approveFunction = new Function("setApprovalForAll",
                Arrays.asList(new Address(blockchainService.getMarketplace().getContractAddress()), new Bool(true)),
                Collections.emptyList());
            String encodedApprove = FunctionEncoder.encode(approveFunction);

            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setDescription("전자서명 처리 중 — NFT 전송 승인 (ticketId=%d, nftId=%d, fromUserId=%d, toUserId=%d)"
                .formatted(ticketId, onChainTicketNftId, senderUserId, receiverUserId));
            transactionRepository.save(tx);
            log.info("[양도 비동기] NFT approve 전송 중 — sender={}, Marketplace={}", senderAddress,
                blockchainService.getMarketplace().getContractAddress());

            EthSendTransaction approveTx = senderTxManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                blockchainService.getTicketNFT().getContractAddress(), encodedApprove, BigInteger.ZERO);

            if (approveTx.hasError()) {
                throw new BlockchainException("NFT approve 실패: " + approveTx.getError().getMessage());
            }

            // approve TX 블록 확정 대기
            waitForTransaction(approveTx.getTransactionHash());
            log.info("[양도 비동기] NFT approve 블록 확정 완료");

            // ========== ③ Transaction 상태 업데이트 ==========
            tx.setDescription("블록 생성 대기 중 — NFT 소유권 이전 트랜잭션 전파 (ticketId=%d, nftId=%d, approveTxHash=%s)"
                .formatted(ticketId, onChainTicketNftId, approveTx.getTransactionHash()));
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            transactionRepository.save(tx);
            log.info("[양도 비동기] Transaction → SUBMITTED");

            // ========== ④ Marketplace.directTransfer() 호출 ==========
            // 플랫폼 키로 서명 (onlyOwner)
            // 구매자가 approve 했으므로 Marketplace가 transferFrom 가능
            log.info("[양도 비동기] directTransfer 호출 — from={}, to={}, nftId={}", senderAddress, receiverAddress,
                onChainTicketNftId);

            TransactionReceipt receipt = blockchainService.getMarketplace()
                .directTransfer(senderAddress, receiverAddress, BigInteger.valueOf(onChainTicketNftId)).send();

            String txHash = receipt.getTransactionHash();
            log.info("[양도 비동기] directTransfer 블록 확정 완료 — txHash={}", txHash);

            // ========== ④ DB 업데이트 ==========
            // Ticket 소유자 변경: 보내는 사람 → 받는 사람
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
            Long previousOwner = ticket.getUserId();
            ticket.setUserId(receiverUserId);
            ticketRepository.save(ticket);
            log.info("[양도 비동기] Ticket 소유자 변경 — ticketId={}, {} → {}", ticketId, previousOwner, receiverUserId);

            // Transaction → CONFIRMED
            tx.setTxHash(txHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription("블록체인 확정 — 양도 완료 (ticketId=%d, nftId=%d, fromUserId=%d, toUserId=%d, txHash=%s)"
                .formatted(ticketId, onChainTicketNftId, senderUserId, receiverUserId, txHash));
            transactionRepository.save(tx);

            TicketTransfer ticketTransfer = TicketTransfer.builder()
                .fromUserId(senderUserId)
                .toUserId(receiverUserId)
                .ticketId(ticketId)
                .txHash(txHash)
                .build();
            ticketTransferRepository.save(ticketTransfer);

            log.info("[양도 비동기] Transaction → CONFIRMED — txId={}", txId);

            log.info("[양도 비동기] 완료 — txId={}, ticketId={}, sender={} → receiver={}", txId, ticketId, senderUserId,
                receiverUserId);

        } catch (BlockchainException e) {
            log.error("[양도 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[양도 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, "양도 실패 — " + e.getMessage());
        }
    }

    // ==================== 티켓 환불 ====================

    /**
     * 블록체인 환불 처리 — @Async로 별도 스레드에서 실행
     *
     * [흐름] ① 환불 대상(ticket/seat/user) 재검증 + 온체인 소유자 검증 ② 사용자 키로
     * TicketNFT.setApprovalForAll(Settlement, true) — NFT 회수 허락 ③
     * Settlement.refund() 호출 — 온체인에서 환불액 계산 + SSF 전송 + NFT 회수 ④ 온체인 NFT 회수 검증 ⑤
     * Transaction/Ticket/Seat 상태 반영
     */
    @Async
    public void processOnChainRefund(Long txId, Long userId, Long ticketId, Long onChainSessionId,
        Long onChainTicketNftId) {
        log.info("[티켓 환불 비동기] 시작 — txId={}, ticketId={}, userId={}", txId, ticketId, userId);

        Long sessionSeatId = null;
        try {
            // ========== ① 환불 대상 재검증 + 지갑 로드 ==========
            Transaction tx = transactionRepository.findById(txId).orElseThrow();

            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BlockchainException("환불 대상 티켓을 찾을 수 없습니다."));

            if (!ticket.getUserId().equals(userId)) {
                throw new BlockchainException("환불 요청자와 티켓 소유자가 일치하지 않습니다.");
            }
            if (ticket.getTicketNftId() == null || !ticket.getTicketNftId().equals(onChainTicketNftId)) {
                throw new BlockchainException("온체인 티켓 정보가 올바르지 않습니다.");
            }

            sessionSeatId = ticket.getSessionSeatId();
            SessionSeat sessionSeat = sessionSeatRepository.findById(sessionSeatId)
                .orElseThrow(() -> new BlockchainException("환불 대상 좌석을 찾을 수 없습니다."));
            if (sessionSeat.getStatus() != SeatStatus.PENDING_TX) {
                throw new BlockchainException("환불 가능한 좌석 상태가 아닙니다.");
            }

            User owner = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BlockchainException("사용자를 찾을 수 없습니다."));
            Wallet ownerWallet = walletRepository.findById(owner.getWalletId())
                .orElseThrow(() -> new BlockchainException("사용자 지갑을 찾을 수 없습니다."));
            String ownerAddress = ownerWallet.getAddress();

            log.info("[티켓 환불 비동기] 사용자 지갑 로드 — userId={}, address={}", userId, ownerAddress);

            BigInteger sessionIdValue = BigInteger.valueOf(onChainSessionId);
            BigInteger ticketNftIdValue = BigInteger.valueOf(onChainTicketNftId);

            // 온체인 소유자 검증
            String currentOnChainOwner = blockchainService.getTicketNFT().ownerOf(ticketNftIdValue).send();
            if (!currentOnChainOwner.equalsIgnoreCase(ownerAddress)) {
                throw new BlockchainException("온체인 티켓 소유자가 환불 요청자와 일치하지 않습니다.");
            }

            // ========== ② TicketNFT.setApprovalForAll(Settlement, true) — 사용자 키로 대리 서명
            // ==========
            Credentials ownerCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + ownerWallet.getKeystoreFilename()));

            RawTransactionManager ownerTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                ownerCredentials, blockchainService.getChainId());

            String settlementAddress = blockchainService.getSettlement().getContractAddress();

            tx.setDescription("전자서명 처리 중 — NFT 환불 승인 (ticketId=%d, nftId=%d, userId=%d)".formatted(ticketId,
                onChainTicketNftId, userId));
            transactionRepository.save(tx);
            log.info("[티켓 환불 비동기] NFT approve 전송 중 — Settlement={}", settlementAddress);

            EthSendTransaction approveTx = sendSetApprovalForAllTransaction(ownerTxManager,
                blockchainService.getTicketNFT().getContractAddress(), settlementAddress);

            if (approveTx.hasError()) {
                throw new BlockchainException("NFT approve 실패: " + approveTx.getError().getMessage());
            }

            waitForTransaction(approveTx.getTransactionHash());
            log.info("[티켓 환불 비동기] NFT approve 블록 확정 완료");

            boolean approvedForSettlement = isApprovedForAll(ownerAddress, settlementAddress);
            if (!approvedForSettlement) {
                throw new BlockchainException("Settlement에 대한 NFT 전송 권한 승인이 완료되지 않았습니다.");
            }

            // ========== ③ Settlement.refund() 호출 ==========
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            tx.setDescription("블록 생성 대기 중 — 티켓 환불 트랜잭션 전파 (ticketId=%d, nftId=%d, approveTxHash=%s)".formatted(ticketId,
                onChainTicketNftId, approveTx.getTransactionHash()));
            transactionRepository.save(tx);
            log.info("[티켓 환불 비동기] Transaction → SUBMITTED");

            TransactionReceipt refundReceipt = blockchainService.getSettlement()
                .refund(sessionIdValue, ownerAddress, ticketNftIdValue).send();
            String txHash = refundReceipt.getTransactionHash();
            log.info("[티켓 환불 비동기] Settlement.refund 블록 확정 — txHash={}", txHash);

            // ========== ④ 온체인 NFT 회수 검증 ==========
            String newOnChainOwner = blockchainService.getTicketNFT().ownerOf(ticketNftIdValue).send();
            if (!newOnChainOwner.equalsIgnoreCase(blockchainService.getPlatformWalletAddress())) {
                throw new BlockchainException("환불 후 NFT 소유권 이전이 완료되지 않았습니다.");
            }

            // 온체인 이벤트에서 실제 환불 금액 추출 (온체인이 source of truth)
            long refundedAmount = 0;
            var refundedEvents = com.ssafy.cheket.blockchain.contract.Settlement.getRefundedEvents(refundReceipt);
            if (!refundedEvents.isEmpty() && refundedEvents.get(0).amount != null) {
                refundedAmount = refundedEvents.get(0).amount.longValue();
            }

            // ========== ⑤ Transaction/Ticket/Seat 상태 반영 ==========
            tx.setAmount(refundedAmount);
            tx.setTxHash(txHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setBlockNumber(
                refundReceipt.getBlockNumber() != null ? refundReceipt.getBlockNumber().longValue() : null);
            tx.setDescription("블록체인 확정 — 환불 완료 (ticketId=%d, nftId=%d, refunded=%d SSF, txHash=%s)".formatted(ticketId,
                onChainTicketNftId, refundedAmount, txHash));
            transactionRepository.save(tx);

            ticketRepository.delete(ticket);

            sessionSeat.setStatus(SeatStatus.AVAILABLE);
            sessionSeatRepository.save(sessionSeat);

            log.info("[티켓 환불 비동기] 완료 — txId={}, ticketId={}, refunded={} SSF", txId, ticketId, refundedAmount);

        } catch (BlockchainException e) {
            log.error("[티켓 환불 비동기] 실패 — txId={}, ticketId={}", txId, ticketId, e);
            restoreRefundSeatToSold(sessionSeatId, txId);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[티켓 환불 비동기] 실패 — txId={}, ticketId={}", txId, ticketId, e);
            restoreRefundSeatToSold(sessionSeatId, txId);
            updateTransactionFailed(txId, "티켓 환불 실패 — " + e.getMessage());
        }
    }

    /**
     * TicketNFT.setApprovalForAll(operator, true) TX 전송
     */
    private EthSendTransaction sendSetApprovalForAllTransaction(RawTransactionManager txManager,
        String nftContractAddress, String operatorAddress) throws Exception {
        Function approveFunction = new Function("setApprovalForAll",
            Arrays.asList(new Address(operatorAddress), new Bool(true)), Collections.emptyList());

        String encodedApprove = FunctionEncoder.encode(approveFunction);

        return txManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000), nftContractAddress,
            encodedApprove, BigInteger.ZERO);
    }

    /**
     * owner가 operator에게 전체 NFT 이동 권한을 줬는지 확인
     */
    private boolean isApprovedForAll(String ownerAddress, String operatorAddress) throws Exception {
        return Boolean.TRUE
            .equals(blockchainService.getTicketNFT().isApprovedForAll(ownerAddress, operatorAddress).send());
    }

    // ==================== StakeholderNFT 발행 ====================

    /**
     * StakeholderNFT 온체인 발행 — @Async로 별도 스레드에서 실행 [흐름] ① Stakeholder별 지갑 주소 조회 ② 지갑
     * 주소 유효성 검증 ③ StakeholderNFT.mint(지갑주소, 역할, shareBps, 0) 호출 ④
     * TransactionReceipt에서 tokenId 추출 → DB 저장 ⑤ Transaction → CONFIRMED / FAILED
     */
    @Async
    public void processOnChainStakeholderMint(Long txId, List<Long> stakeholderIds, String platformWallet) {
        log.info("[StakeholderNFT 발행 비동기] 시작 — txId={}, stakeholders={}", txId, stakeholderIds.size());

        try {
            List<Stakeholder> stakeholders = stakeholderRepository.findAllById(stakeholderIds);

            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            transactionRepository.save(tx);

            String lastTxHash = null;

            for (int i = 0; i < stakeholders.size(); i++) {
                Stakeholder stakeholder = stakeholders.get(i);

                tx.setDescription("블록 확정 대기 — 이해관계자 등록 (%d/%d, stakeholderId=%d)".formatted(i + 1, stakeholders.size(),
                    stakeholder.getId()));
                transactionRepository.save(tx);

                // ========== ① 이해관계자 지갑 주소 조회 ==========
                String walletAddress;
                if (stakeholder.getHostId() != null) {
                    Host stakeholderHost = hostRepository.findById(stakeholder.getHostId())
                        .orElseThrow(() -> new BlockchainException("호스트를 찾을 수 없습니다: " + stakeholder.getHostId()));
                    walletAddress = walletRepository.findById(stakeholderHost.getWalletId())
                        .orElseThrow(() -> new BlockchainException("지갑을 찾을 수 없습니다")).getAddress();
                } else if (stakeholder.getUserId() != null) {
                    User user = userRepository.findByIdAndDeletedAtIsNull(stakeholder.getUserId())
                        .orElseThrow(() -> new BlockchainException("유저를 찾을 수 없습니다: " + stakeholder.getUserId()));
                    walletAddress = walletRepository.findById(user.getWalletId())
                        .orElseThrow(() -> new BlockchainException("지갑을 찾을 수 없습니다")).getAddress();
                } else {
                    // 플랫폼 Stakeholder (hostId, userId 모두 null)
                    walletAddress = platformWallet;
                }

                // ========== ② 지갑 주소 유효성 검증 ==========
                if (!walletAddress.matches("^0x[0-9a-fA-F]{40}$")) {
                    throw new BlockchainException("유효하지 않은 지갑 주소: " + walletAddress);
                }

                // ========== ③ 온체인 StakeholderNFT.mint() 호출 ==========
                String role = stakeholder.getRole().name().toLowerCase();
                BigInteger shareBps = BigInteger.valueOf(stakeholder.getShareBps());
                BigInteger eventNftId = BigInteger.ZERO; // EventNFT 미발행 → 0

                log.info("[StakeholderNFT 발행 비동기] mint 호출 — {}/{}, wallet={}, role={}, shareBps={}", i + 1,
                    stakeholders.size(), walletAddress, role, shareBps);

                TransactionReceipt receipt = blockchainService.getStakeholderNFT()
                    .mint(walletAddress, role, shareBps, eventNftId).send();

                lastTxHash = receipt.getTransactionHash();

                // ========== ④ TransactionReceipt에서 tokenId 추출 → DB 저장 ==========
                List<?> events = blockchainService.getStakeholderNFT().getStakeholderMintedEvents(receipt);

                if (!events.isEmpty()) {
                    Object event = events.get(0);
                    BigInteger tokenId = ((StakeholderMintedEventResponse) event).tokenId;

                    stakeholder.setStakeholderNftId(tokenId.longValue());
                    stakeholderRepository.save(stakeholder);

                    log.info("[StakeholderNFT 발행 비동기] 발행 완료 — tokenId={}, wallet={}, role={}, shareBps={}", tokenId,
                        walletAddress, role, shareBps);
                }
            }

            // ========== ⑤ Transaction → CONFIRMED ==========
            tx.setTxHash(lastTxHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription("블록체인 확정 — 수익 분배 계약 등록 완료 (%d건, txHash=%s)".formatted(stakeholders.size(), lastTxHash));
            transactionRepository.save(tx);
            log.info("[StakeholderNFT 발행 비동기] 완료 — txId={}, {}건", txId, stakeholders.size());

        } catch (BlockchainException e) {
            log.error("[StakeholderNFT 발행 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[StakeholderNFT 발행 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, "공연 등록 실패 — " + e.getMessage());
        }
    }

    // ==================== 공통 유틸 ====================

    /**
     * TX가 블록에 포함될 때까지 대기 (최대 30초) 1초마다 블록체인에 "이 TX 블록에 들어갔나?" 확인
     */
    private void waitForTransaction(String txHash) {
        try {
            int maxAttempts = 30;
            for (int i = 0; i < maxAttempts; i++) {
                var receipt = blockchainService.getWeb3j().ethGetTransactionReceipt(txHash).send()
                    .getTransactionReceipt();
                if (receipt.isPresent()) {
                    return;
                }
                Thread.sleep(1000);
            }
            throw new BlockchainException("TX 확정 대기 타임아웃: " + txHash);
        } catch (BlockchainException e) {
            throw e;
        } catch (Exception e) {
            throw new BlockchainException("TX 확정 대기 실패: " + txHash + " — " + e.getMessage());
        }
    }

    /**
     * 티켓 번호 생성 (고유 식별자) QR 코드나 앱에서 표시용 (DB id보다 사용자 친화적)
     */
    /**
     * Keep local wallet balance in sync with on-chain state after balance-changing
     * transactions.
     */
    private void syncWalletBalanceFromChain(Wallet wallet) {
        try {
            int onChainBalance = blockchainService.getSsfBalance(wallet.getAddress()).intValue();
            wallet.setCtkBalance(onChainBalance);
            wallet.setBalanceSyncedAt(LocalDateTime.now());
            walletRepository.save(wallet);
        } catch (Exception e) {
            log.warn("[잔액 동기화] walletId={} on-chain sync failed", wallet.getId(), e);
        }
    }

    private String generateTicketNumber() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 좌석 PENDING_TX → AVAILABLE 복원 (구매 실패 시) 이미 SOLD된 좌석은 건드리지 않음
     */
    private void restoreSeatsToAvailable(List<Long> sessionSeatIds, Long txId) {
        try {
            List<SessionSeat> freshSeats = sessionSeatRepository.findAllById(sessionSeatIds);
            for (SessionSeat seat : freshSeats) {
                if (seat.getStatus() == SeatStatus.PENDING_TX) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                }
            }
            sessionSeatRepository.saveAll(freshSeats);
            log.info("[비동기] 좌석 AVAILABLE 복원 완료 — txId={}", txId);
        } catch (Exception seatErr) {
            log.error("[비동기] 좌석 복원 실패 — txId={}", txId, seatErr);
        }
    }

    /**
     * 환불 실패 시 좌석 상태를 SOLD로 복원
     */
    private void restoreRefundSeatToSold(Long sessionSeatId, Long txId) {
        if (sessionSeatId == null) {
            return;
        }
        try {
            SessionSeat seat = sessionSeatRepository.findById(sessionSeatId).orElse(null);
            if (seat != null && seat.getStatus() == SeatStatus.PENDING_TX) {
                seat.setStatus(SeatStatus.SOLD);
                sessionSeatRepository.save(seat);
                log.info("[환불 비동기] 좌석 상태 복원 완료 — txId={}, sessionSeatId={}", txId, sessionSeatId);
            }
        } catch (Exception e) {
            log.error("[환불 비동기] 좌석 상태 복원 실패 — txId={}, sessionSeatId={}", txId, sessionSeatId, e);
        }
    }

    /**
     * 리세일 등록 실패 시 Ticket.resaleStatus → AVAILABLE 복원
     */
    private void restoreResaleTicketToAvailable(Long ticketId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket != null && ticket.getResaleStatus() == ResaleStatus.LISTED) {
                ticket.setResaleStatus(ResaleStatus.AVAILABLE);
                ticketRepository.save(ticket);
                log.info("[리세일 등록 비동기] Ticket resaleStatus AVAILABLE 복원 — ticketId={}", ticketId);
            }
        } catch (Exception e) {
            log.error("[리세일 등록 비동기] Ticket 복원 실패 — ticketId={}", ticketId, e);
        }
    }

    /**
     * 구매 성공한 좌석 정보 (에러 메시지에 좌석 정보 포함용)
     */
    private static class PurchasedSeatInfo {
        final SessionSeat seat;
        final Ticket ticket;
        final BigInteger ticketNftId;

        PurchasedSeatInfo(SessionSeat seat, Ticket ticket, BigInteger ticketNftId) {
            this.seat = seat;
            this.ticket = ticket;
            this.ticketNftId = ticketNftId;
        }
    }

    // ==================== 리세일 등록 ====================

    /**
     * 리세일 등록 — Escrow.createDeal()로 NFT 예치
     *
     * [양도와의 차이] 양도: NFT를 받는사람에게 직접 전송 (Marketplace.directTransfer) 리세일: NFT를
     * Escrow에 예치 → 구매자가 나타나면 Escrow가 정산
     *
     * [흐름] ① 판매자 키로 TicketNFT.setApprovalForAll(Escrow, true) — NFT 예치 허락 ②
     * Escrow.createDeal(seller, ticketId, price, deadline) — NFT 예치 + 가격 상한 검증 ③
     * 이벤트에서 dealId 추출 → DB Resale 레코드 생성
     */
    @Async
    public void processOnChainResaleCreate(Long txId, Long userId, Long ticketId, Long onChainTicketNftId,
        int resalePrice, long deadlineUnix, int originalPrice) {
        log.info("[리세일 등록 비동기] 시작 — txId={}, ticketNftId={}, price={}", txId, onChainTicketNftId, resalePrice);

        try {
            // ① 판매자 지갑 로드
            User seller = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BlockchainException("판매자를 찾을 수 없습니다."));
            Wallet sellerWallet = walletRepository.findById(seller.getWalletId())
                .orElseThrow(() -> new BlockchainException("판매자 지갑을 찾을 수 없습니다."));
            String sellerAddress = sellerWallet.getAddress();
            log.info("[리세일 등록 비동기] 판매자 지갑 — userId={}, address={}", userId, sellerAddress);

            // ② TicketNFT.setApprovalForAll(Escrow, true) — 판매자 키로 서명
            Credentials sellerCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + sellerWallet.getKeystoreFilename()));

            RawTransactionManager sellerTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                sellerCredentials, blockchainService.getChainId());

            Function approveFunction = new Function("setApprovalForAll",
                Arrays.asList(new Address(blockchainService.getEscrow().getContractAddress()), new Bool(true)),
                Collections.emptyList());
            String encodedApprove = FunctionEncoder.encode(approveFunction);

            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setDescription("전자서명 처리 중 — NFT 예치 승인 (ticketId=%d, nftId=%d, sellerUserId=%d, price=%d SSF)"
                .formatted(ticketId, onChainTicketNftId, userId, resalePrice));
            transactionRepository.save(tx);
            log.info("[리세일 등록 비동기] NFT approve 전송 중");

            EthSendTransaction approveTx = sellerTxManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                blockchainService.getTicketNFT().getContractAddress(), encodedApprove, BigInteger.ZERO);

            if (approveTx.hasError()) {
                throw new BlockchainException("NFT approve 실패: " + approveTx.getError().getMessage());
            }

            waitForTransaction(approveTx.getTransactionHash());
            log.info("[리세일 등록 비동기] NFT approve 블록 확정 완료");

            // ③ Escrow.createDeal() — 플랫폼 키로 서명
            tx.setDescription("블록 생성 대기 중 — 리세일 등록 트랜잭션 전파 (ticketId=%d, approveTxHash=%s, deadline=%d)"
                .formatted(ticketId, approveTx.getTransactionHash(), deadlineUnix));
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            transactionRepository.save(tx);
            log.info("[리세일 등록 비동기] Transaction → SUBMITTED");

            log.info("[리세일 등록 비동기] createDeal 호출 — seller={}, nftId={}, price={}, deadline={}", sellerAddress,
                onChainTicketNftId, resalePrice, deadlineUnix);

            TransactionReceipt receipt = blockchainService.getEscrow()
                .createDeal(sellerAddress, BigInteger.valueOf(onChainTicketNftId), BigInteger.valueOf(resalePrice),
                    BigInteger.valueOf(deadlineUnix))
                .send();

            String txHash = receipt.getTransactionHash();
            log.info("[리세일 등록 비동기] createDeal 블록 확정 — txHash={}", txHash);

            // ④ 이벤트에서 dealId 추출
            List<?> events = blockchainService.getEscrow().getDealCreatedEvents(receipt);
            Long onChainDealId = null;
            if (!events.isEmpty()) {
                Object event = events.get(0);
                onChainDealId = ((com.ssafy.cheket.blockchain.contract.Escrow.DealCreatedEventResponse) event).dealId
                    .longValue();
                log.info("[리세일 등록 비동기] onChainDealId={}", onChainDealId);
            }

            // ⑤ Resale 레코드 생성
            Resale resale = Resale.builder().ticketId(ticketId).userId(userId).originalPrice(originalPrice)
                .resalePrice(resalePrice).status(Resale.ResaleListingStatus.ACTIVE).txHash(txHash)
                .onChainListingId(onChainDealId != null ? onChainDealId.intValue() : null).build();
            resaleEntityRepository.save(resale);
            log.info("[리세일 등록 비동기] Resale 레코드 생성 — resaleId={}", resale.getId());

            // ⑥ Transaction → CONFIRMED
            tx.setTxHash(txHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription("블록체인 확정 — 리세일 등록 완료 (ticketId=%d, dealId=%s, price=%d SSF, txHash=%s)"
                .formatted(ticketId, onChainDealId, resalePrice, txHash));
            transactionRepository.save(tx);

            log.info("[리세일 등록 비동기] 완료 — txId={}, dealId={}", txId, onChainDealId);

        } catch (BlockchainException e) {
            log.error("[리세일 등록 비동기] 실패 — txId={}", txId, e);
            restoreResaleTicketToAvailable(ticketId);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[리세일 등록 비동기] 실패 — txId={}", txId, e);
            restoreResaleTicketToAvailable(ticketId);
            updateTransactionFailed(txId, "리세일 등록 실패 — " + e.getMessage());
        }
    }

    /**
     * 리세일 취소 — Escrow.cancelDeal()로 NFT 반환
     *
     * [등록과의 차이] 등록: approve(1 TX) + createDeal(1 TX) = 2 TX 취소: cancelDeal(1 TX) =
     * 1 TX만 → Escrow가 이미 NFT를 갖고 있으니 approve 없이 반환만
     *
     * [Escrow.cancelDeal() 컨트랙트 내부 동작] ① deal.status → CANCELLED ②
     * ticketNFT.transferFrom(Escrow → 판매자) — NFT 반환 ③ walletTicketCount 복원 (판매자 +1)
     * → 하나라도 실패하면 전부 revert (원자적)
     */
    @Async
    public void processOnChainResaleCancel(Long txId, Long ticketId, Long resaleId, int onChainDealId) {
        log.info("[리세일 취소 비동기] 시작 — txId={}, dealId={}", txId, onChainDealId);

        try {
            // ① Transaction 상태 업데이트
            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setDescription("블록체인 준비 중 — NFT 반환 대기 (ticketId=%d, dealId=%d)".formatted(ticketId, onChainDealId));
            transactionRepository.save(tx);

            tx.setDescription(
                "블록 생성 대기 중 — NFT 반환 트랜잭션 전파 (ticketId=%d, dealId=%d)".formatted(ticketId, onChainDealId));
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            transactionRepository.save(tx);
            log.info("[리세일 취소 비동기] Transaction → SUBMITTED");

            // ② Escrow.cancelDeal() — 플랫폼 키로 서명
            log.info("[리세일 취소 비동기] cancelDeal 호출 — dealId={}", onChainDealId);

            TransactionReceipt receipt = blockchainService.getEscrow().cancelDeal(BigInteger.valueOf(onChainDealId))
                .send();

            String txHash = receipt.getTransactionHash();
            log.info("[리세일 취소 비동기] cancelDeal 블록 확정 — txHash={}", txHash);

            // ③ DB 업데이트: Resale → CANCELLED
            Resale resale = resaleEntityRepository.findById(resaleId).orElseThrow();
            resale.setStatus(Resale.ResaleListingStatus.CANCELLED);
            resale.setUpdatedAt(java.time.LocalDateTime.now());
            resaleEntityRepository.save(resale);
            log.info("[리세일 취소 비동기] Resale → CANCELLED — resaleId={}", resaleId);

            // ④ DB 업데이트: Ticket → AVAILABLE
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
            ticket.setResaleStatus(ResaleStatus.AVAILABLE);
            ticketRepository.save(ticket);
            log.info("[리세일 취소 비동기] Ticket → AVAILABLE — ticketId={}", ticketId);

            // ⑤ Transaction → CONFIRMED
            tx.setTxHash(txHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription(
                "블록체인 확정 — 리세일 취소 완료 (ticketId=%d, dealId=%d, txHash=%s)".formatted(ticketId, onChainDealId, txHash));
            transactionRepository.save(tx);

            log.info("[리세일 취소 비동기] 완료 — txId={}, dealId={}", txId, onChainDealId);

        } catch (BlockchainException e) {
            log.error("[리세일 취소 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[리세일 취소 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, "리세일 취소 실패 — " + e.getMessage());
        }
    }

    // ==================== 리세일 구매 ====================

    /**
     * 리세일 구매 — Escrow.buyAndSettle()로 SSF 전송 + NFT 이전
     *
     * [1차 구매와의 차이] 1차: SSF → Settlement (자금 잠금, 나중에 정산) 리세일: SSF → 판매자 직접 전송 (정산
     * 없음, Escrow에 SSF 체류 없음)
     *
     * [Escrow.buyAndSettle() 컨트랙트 내부 동작] ① SSF: 구매자 → 판매자 직접 전송 (transferFrom) ②
     * NFT: Escrow → 구매자 (transferFrom) ③ walletTicketCount 갱신 (구매자 +1) ④
     * deal.status → SOLD → 하나라도 실패하면 전부 revert (원자적)
     */
    @Async
    public void processOnChainResalePurchase(Long txId, Long buyerUserId, Long sellerUserId, Long ticketId,
        Long resaleId, int onChainDealId, int resalePrice) {
        log.info("[리세일 구매 비동기] 시작 — txId={}, dealId={}, price={}", txId, onChainDealId, resalePrice);

        try {
            // ① 구매자 지갑 로드
            User buyer = userRepository.findByIdAndDeletedAtIsNull(buyerUserId)
                .orElseThrow(() -> new BlockchainException("구매자를 찾을 수 없습니다."));

            Wallet buyerWallet = walletRepository.findById(buyer.getWalletId())
                .orElseThrow(() -> new BlockchainException("구매자 지갑을 찾을 수 없습니다."));

            String buyerAddress = buyerWallet.getAddress();
            log.info("[리세일 구매 비동기] 구매자 지갑 — userId={}, address={}", buyerUserId, buyerAddress);

            // ② SSF.approve(Escrow, price) — 구매자 키로 서명
            // "Escrow가 내 SSF를 resalePrice만큼 가져가도 돼"
            Credentials buyerCredentials = WalletUtils.loadCredentials(keystorePassword,
                new File(keystoreDirectory + "/" + buyerWallet.getKeystoreFilename()));

            RawTransactionManager buyerTxManager = new RawTransactionManager(blockchainService.getWeb3j(),
                buyerCredentials, blockchainService.getChainId());

            Function approveFunction = new Function("approve",
                Arrays.asList(new Address(blockchainService.getEscrow().getContractAddress()),
                    new Uint256(BigInteger.valueOf(resalePrice))),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
            String encodedApprove = FunctionEncoder.encode(approveFunction);

            Transaction tx = transactionRepository.findById(txId).orElseThrow();
            tx.setDescription("전자서명 처리 중 — SSF 결제 승인 (buyerUserId=%d, dealId=%d, price=%d SSF)".formatted(buyerUserId,
                onChainDealId, resalePrice));
            transactionRepository.save(tx);
            log.info("[리세일 구매 비동기] SSF approve 전송 중 — Escrow={}, resalePrice={}",
                blockchainService.getEscrow().getContractAddress(), resalePrice);

            EthSendTransaction approveTx = buyerTxManager.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(100000),
                blockchainService.getSsfContractAddress(), encodedApprove, BigInteger.ZERO);

            if (approveTx.hasError()) {
                throw new BlockchainException("SSF approve 실패: " + approveTx.getError().getMessage());
            }

            waitForTransaction(approveTx.getTransactionHash());
            log.info("[리세일 구매 비동기] SSF approve 블록 확정 완료");

            // ③ Escrow.buyAndSettle() — 플랫폼 키로 서명
            tx.setDescription("블록 생성 대기 중 — 리세일 구매 트랜잭션 전파 (dealId=%d, approveTxHash=%s)".formatted(onChainDealId,
                approveTx.getTransactionHash()));
            tx.setTxStatus(Transaction.TxStatus.SUBMITTED);
            tx.setTxHash(approveTx.getTransactionHash());
            transactionRepository.save(tx);
            log.info("[리세일 구매 비동기] Transaction → SUBMITTED");

            log.info("[리세일 구매 비동기] buyAndSettle 호출 — dealId={}, buyer={}", onChainDealId, buyerAddress);

            TransactionReceipt receipt = blockchainService.getEscrow()
                .buyAndSettle(buyerAddress, BigInteger.valueOf(onChainDealId)).send();

            String txHash = receipt.getTransactionHash();
            log.info("[리세일 구매 비동기] buyAndSettle 블록 확정 — txHash={}", txHash);

            // ④ DB 업데이트: Resale → SOLD
            Resale resale = resaleEntityRepository.findById(resaleId).orElseThrow();
            resale.setStatus(Resale.ResaleListingStatus.SOLD);
            resale.setBuyerId(buyerUserId);
            resale.setUpdatedAt(java.time.LocalDateTime.now());
            resaleEntityRepository.save(resale);
            log.info("[리세일 구매 비동기] Resale → SOLD — resaleId={}", resaleId);

            // ⑤ DB 업데이트: Ticket 소유자 변경 + resaleStatus
            Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
            ticket.setUserId(buyerUserId);
            ticket.setResaleStatus(ResaleStatus.AVAILABLE);
            ticketRepository.save(ticket);
            log.info("[리세일 구매 비동기] Ticket 소유자 변경 — ticketId={}, seller={} → buyer={}", ticketId, sellerUserId,
                buyerUserId);

            // ⑥ 구매자 Transaction → CONFIRMED
            tx.setTxHash(txHash);
            tx.setTxStatus(Transaction.TxStatus.CONFIRMED);
            tx.setDescription(
                "블록체인 확정 — 리세일 구매 완료 (ticketId=%d, dealId=%d, buyerUserId=%d, sellerUserId=%d, price=%d SSF, txHash=%s)"
                    .formatted(ticketId, onChainDealId, buyerUserId, sellerUserId, resalePrice, txHash));
            transactionRepository.save(tx);

            SessionSeat sessionSeat = sessionSeatRepository.findById(ticket.getSessionSeatId())
                .orElseThrow(() -> new NotFoundException("좌석 정보를 찾을 수 없습니다."));

            Session session = sessionRepository.findById(sessionSeat.getSessionId())
                .orElseThrow(() -> new NotFoundException("회차를 찾을 수 없습니다."));

            Show show = showRepository.findById(session.getShowId())
                .orElseThrow(() -> new NotFoundException("공연을 찾을 수 없습니다."));

            // 판매자 알림
            notificationService.sendResale(sellerUserId, show.getTitle());

            // RESALE_PURCHASE 1건에 buyerId + sellerId 둘 다 있으므로
            // 구매자/판매자 모두 거래내역에서 조회 가능 (별도 TX 불필요)

            log.info("[리세일 구매 비동기] 완료 — txId={}, dealId={}", txId, onChainDealId);

        } catch (BlockchainException e) {
            log.error("[리세일 구매 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, e.getMessage());
        } catch (Exception e) {
            log.error("[리세일 구매 비동기] 실패 — txId={}", txId, e);
            updateTransactionFailed(txId, "리세일 구매 실패 — " + e.getMessage());
        }
    }

    // ==================== 공통 유틸 ====================

    /**
     * Transaction → FAILED 업데이트 (공통)
     */
    private void updateTransactionFailed(Long txId, String description) {
        try {
            Transaction failedTx = transactionRepository.findById(txId).orElseThrow();
            failedTx.setTxStatus(Transaction.TxStatus.FAILED);
            String previousDescription = failedTx.getDescription();
            if (previousDescription == null || previousDescription.isBlank()) {
                failedTx.setDescription("FAILED — " + description);
            } else {
                failedTx.setDescription(previousDescription + " | FAILED: " + description);
            }
            transactionRepository.save(failedTx);
            log.info("[비동기] Transaction FAILED 업데이트 완료 — txId={}", txId);
        } catch (Exception dbErr) {
            log.error("[비동기] Transaction FAILED 업데이트 실패 — txId={}", txId, dbErr);
        }
    }
}
