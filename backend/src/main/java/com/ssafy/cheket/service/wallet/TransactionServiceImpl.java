package com.ssafy.cheket.service.wallet;

import com.ssafy.cheket.dto.wallet.response.TransactionResponse;
import com.ssafy.cheket.entity.settlement.Settlement;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.transaction.Transaction.TransactionType;
import com.ssafy.cheket.entity.transaction.Transaction.TxStatus;
import com.ssafy.cheket.enums.StakeholderRole;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.repository.settlement.SettlementRepository;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.cheket.exception.common.NotFoundException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 트랜잭션 상태 관리 구현체
 *
 * [사용처] - WalletServiceImpl: tx 생성(PENDING), 제출(SUBMITTED), 실패(FAILED) -
 * SsfTransferEventListener: 블록 확정(CONFIRMED)
 *
 * [왜 별도 서비스인가?] 상태 전이 로직을 한 곳에 집중시켜서: 1. @Transactional로 DB 일관성 보장 2. 여러 곳에서
 * 호출해도 같은 방식으로 상태 변경 3. 나중에 상태 변경 시 이벤트 발행(SSE/WebSocket 알림) 추가가 쉬움
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;
    private final StakeholderRepository stakeholderRepository;
    private final ShowRepository showRepository;
    private final SessionRepository sessionRepository;

    private static final DateTimeFormatter SESSION_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    /**
     * 트랜잭션 생성 (PENDING 상태, txHash = null)
     *
     * [호출 시점] WalletServiceImpl.transferInitialFunds()에서 블록체인에 tx를 보내기 "전에" 먼저 DB에
     * 레코드를 만든다. → 이렇게 하면 tx 전송 중 서버가 죽어도 PENDING 레코드가 남아서 추적 가능
     *
     * [sellerType 사용 예시] - 초기 전송(TRANSFER): sellerId=null, sellerType=null (플랫폼) -
     * 1차 구매(PURCHASE): sellerId=host.id, sellerType=HOST - 재판매(RESALE):
     * sellerId=user.id, sellerType=USER
     */
    @Override
    @Transactional
    public Transaction createPendingTransaction(TransactionType type, Long amount, String description, Long buyerId,
        Long sellerId) {
        Transaction tx = Transaction.builder().type(type).amount(amount).description(description).txHash(null) // 아직
                                                                                                               // 블록체인에
                                                                                                               // 미전송 →
                                                                                                               // txHash
                                                                                                               // 없음
            .txStatus(TxStatus.PENDING) // 초기 상태
            .buyerId(buyerId).sellerId(sellerId) // 플랫폼이면 null
            .build();
        return transactionRepository.save(tx);
    }

    /**
     * PENDING → SUBMITTED
     *
     * [호출 시점] WalletServiceImpl에서 web3j로 tx를 블록체인에 보내고, 성공적으로 txHash를 받으면 이 메서드를
     * 호출한다.
     *
     * 이 시점에서 tx는 블록체인의 "멤풀(대기열)"에 있는 상태. 아직 블록에 포함되지 않았으므로 "확정"은 아니다. → 이벤트 리스너가
     * 블록에 포함된 걸 확인하면 CONFIRMED로 바뀜
     */
    @Override
    @Transactional
    public Transaction updateToSubmitted(Long transactionId, String txHash) {
        Transaction tx = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        tx.setTxHash(txHash); // 블록체인 tx 해시 저장
        tx.setTxStatus(TxStatus.SUBMITTED); // 블록체인에 제출됨
        return transactionRepository.save(tx);
    }

    /**
     * SUBMITTED → CONFIRMED
     *
     * [호출 시점] SsfTransferEventListener가 eth_getLogs로 Transfer 이벤트를 감지하면, txHash로 DB
     * 레코드를 찾아서 이 메서드를 호출한다.
     *
     * blockNumber = 이 tx가 포함된 블록 번호 → "언제 확정됐는지" 나중에 추적할 수 있음
     */
    @Override
    @Transactional
    public Transaction updateToConfirmed(Long transactionId, Long blockNumber) {
        Transaction tx = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        tx.setTxStatus(TxStatus.CONFIRMED); // 블록에 포함 확정
        tx.setBlockNumber(blockNumber); // 확정된 블록 번호 기록
        return transactionRepository.save(tx);
    }

    /**
     * → FAILED
     *
     * [호출 시점] WalletServiceImpl에서 tx 전송 시 에러가 발생하면 호출: - RPC 에러 (잔액 부족, nonce 충돌 등)
     * - 네트워크 에러 (블록체인 노드 접속 불가) - 기타 Exception
     *
     * 실패 사유를 description에 추가해서 나중에 원인 파악 가능
     */
    @Override
    @Transactional
    public Transaction updateToFailed(Long transactionId, String reason) {
        Transaction tx = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        tx.setTxStatus(TxStatus.FAILED);
        tx.setDescription(tx.getDescription() + " | FAILED: " + reason); // 실패 사유 추가
        return transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long userId, String type) {
        boolean isSettlementOnly = "SETTLEMENT".equalsIgnoreCase(type);
        boolean isNoFilter = (type == null || type.isEmpty());

        // ── 일반 거래 내역 ──────────────────────────────────────────────────────────
        List<TransactionResponse> txList = List.of();
        if (!isSettlementOnly) {
            List<Transaction> transactions;
            if (isNoFilter) {
                transactions = transactionRepository.findByUserId(userId);
            } else {
                try {
                    TransactionType txType = TransactionType.valueOf(type.toUpperCase());
                    transactions = transactionRepository.findByUserIdAndType(userId, txType);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("잘못된 거래 유형입니다.");
                }
            }
            txList = transactions.stream()
                .map(tx -> new TransactionResponse(tx.getId(), tx.getType().name(), tx.getAmount(), tx.getDescription(),
                    tx.getTxStatus().name(), tx.getTxHash(), tx.getBlockNumber(), tx.getSellerId(), tx.getBuyerId(),
                    tx.getCreatedAt()))
                .toList();
        }

        // ── 정산 이력 (type 없음 or SETTLEMENT 필터) ─────────────────────────────────
        List<TransactionResponse> settlementList = List.of();
        if (isNoFilter || isSettlementOnly) {
            settlementList = settlementRepository.findByUserIdOrHostId(userId).stream().map(this::toSettlementResponse)
                .toList();
        }

        // ── 합산 후 최신순 정렬 ─────────────────────────────────────────────────────
        return Stream.concat(txList.stream(), settlementList.stream())
            .sorted(
                Comparator.comparing(TransactionResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    /**
     * Settlement 엔티티 → TransactionResponse 변환
     * description 예시: "[BTS 월드투어] 2026.03.22 19:00 회차 정산 수령 · 주최자 72%"
     */
    private TransactionResponse toSettlementResponse(Settlement s) {
        // 공연 제목 조회
        String showTitle = showRepository.findById(s.getShowId())
            .map(Show::getTitle)
            .orElse("알 수 없는 공연");

        // 회차 시작 시간 조회
        String sessionTime = sessionRepository.findById(s.getSessionId())
            .map(session -> session.getSessionStartTime().format(SESSION_FMT))
            .orElse("날짜 미상");

        // 역할 및 분배율 조회
        String roleLabel = "플랫폼";
        String shareLabel = "";
        try {
            Stakeholder stakeholder = stakeholderRepository.findById(s.getStakeholderId()).orElse(null);
            if (stakeholder != null) {
                roleLabel = resolveRoleLabel(stakeholder);
                // shareBps 7200 → "72%", 800 → "8%"
                int percent = stakeholder.getShareBps() / 100;
                int fraction = stakeholder.getShareBps() % 100;  // 소수점 처리 (예: 3333 → 33.33%)
                shareLabel = fraction == 0
                    ? " " + percent + "%"
                    : " " + percent + "." + String.format("%02d", fraction) + "%";
            }
        } catch (Exception e) {
            log.warn("[거래내역] stakeholder 조회 실패 — stakeholderId={}: {}", s.getStakeholderId(), e.getMessage());
        }

        String description = "[" + showTitle + "] " + sessionTime + " 회차 정산 수령 · " + roleLabel + shareLabel;

        return new TransactionResponse(
            s.getId(),
            "SETTLEMENT",
            s.getAmount(),
            description,
            "CONFIRMED",
            s.getTxHash(),
            null,   // blockNumber 없음
            null,   // sellerId 없음
            null,   // buyerId 없음
            s.getCreatedAt()
        );
    }

    /**
     * Stakeholder 역할 → 한국어 레이블 변환
     * userId/hostId 둘 다 null이면 플랫폼 (enum에 PLATFORM 없음)
     */
    private String resolveRoleLabel(Stakeholder stakeholder) {
        if (stakeholder.getUserId() == null && stakeholder.getHostId() == null) {
            return "플랫폼";
        }
        if (stakeholder.getRole() == null) {
            return "이해관계자";
        }
        return switch (stakeholder.getRole()) {
            case ORGANIZER -> "주최자";
            case ARTIST    -> "아티스트";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTxStatus(Long txId) {
        Transaction tx = transactionRepository.findById(txId)
            .orElseThrow(() -> new NotFoundException("트랜잭션을 찾을 수 없습니다: " + txId));

        return Map.of("txId", tx.getId(), "status", tx.getTxStatus().name(), "txHash",
            tx.getTxHash() != null ? tx.getTxHash() : "", "amount", tx.getAmount(), "description",
            tx.getDescription() != null ? tx.getDescription() : "");
    }
}
