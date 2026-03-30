package com.ssafy.cheket.service.wallet;

import com.ssafy.cheket.dto.wallet.response.TransactionResponse;
import com.ssafy.cheket.entity.transaction.Transaction;

import java.util.List;
import java.util.Map;

/**
 * 블록체인 트랜잭션 상태 관리 서비스
 *
 * [역할] Transaction 엔티티의 상태 전이를 관리한다. WalletServiceImpl과
 * SsfTransferEventListener가 이 서비스를 통해 상태를 업데이트한다.
 *
 * [상태 전이 흐름] createPendingTransaction() → PENDING (생성, txHash 없음) ↓
 * updateToSubmitted() → SUBMITTED (블록체인에 tx 전송, txHash 저장) ↓
 * updateToConfirmed() → CONFIRMED (이벤트 리스너가 블록 포함 확인) 또는 updateToFailed() →
 * FAILED (RPC 에러, 잔액 부족 등)
 */
public interface TransactionService {
    /**
     * 트랜잭션 생성 (PENDING 상태) 아직 블록체인에 전송하지 않은 상태 — txHash = null
     *
     * @param type
     *            거래 유형 (TRANSFER, PURCHASE, REFUND 등)
     * @param amount
     *            거래 금액 (SSF 단위)
     * @param description
     *            거래 설명 텍스트
     * @param buyerId
     *            구매자 ID (users.id)
     * @param sellerId
     *            판매자 ID (users.id 또는 hosts.id, 플랫폼이면 null)
     */
    Transaction createPendingTransaction(Transaction.TransactionType type, Long amount, String description,
        Long buyerId, Long sellerId);

    /**
     * PENDING → SUBMITTED (블록체인에 tx 제출 후 txHash 저장) WalletServiceImpl에서 tx 전송 성공 시
     * 호출
     */
    Transaction updateToSubmitted(Long transactionId, String txHash);

    /**
     * SUBMITTED → CONFIRMED (이벤트 리스너가 블록 포함 확인 후 호출) SsfTransferEventListener에서 호출
     */
    Transaction updateToConfirmed(Long transactionId, Long blockNumber);

    /**
     * → FAILED (RPC 에러 또는 온체인 revert 시 호출) WalletServiceImpl에서 tx 전송 실패 시 호출
     */
    Transaction updateToFailed(Long transactionId, String reason);

    List<TransactionResponse> getTransactions(Long userId, String type);

    // TX 상태 조회 — 클라이언트 폴링용 (구매/양도/공연등록/환불/리세일 공통)
    Map<String, Object> getTxStatus(Long txId);
}
