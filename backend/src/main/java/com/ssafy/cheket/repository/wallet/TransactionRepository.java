package com.ssafy.cheket.repository.wallet;

import com.ssafy.cheket.entity.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 블록체인 트랜잭션 DB 조회용 Repository
 *
 * [주요 사용처] 1. findByTxHash → SsfTransferEventListener가 이벤트 감지 시, txHash로 DB
 * 레코드를 찾아서 SUBMITTED → CONFIRMED 업데이트 2. findByTxStatus → 관리자가 PENDING/FAILED
 * 상태인 트랜잭션 모니터링 3. findByBuyerId → 특정 유저의 거래 내역 조회 API
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 블록체인 트랜잭션 해시로 조회
     *
     * 이벤트 리스너가 Transfer 이벤트를 감지하면, 이벤트에 포함된 txHash로 DB에서 해당 트랜잭션 레코드를 찾는다. 찾으면 →
     * SUBMITTED 상태를 CONFIRMED로 업데이트 못 찾으면 → 외부에서 직접 보낸 SSF (우리 시스템 외부 전송)
     */
    Optional<Transaction> findByTxHash(String txHash);

    /** 트랜잭션 상태별 조회 (PENDING, SUBMITTED, CONFIRMED, FAILED) */
    List<Transaction> findByTxStatus(Transaction.TxStatus txStatus);

    /** 구매자(buyerId = users.id) 기준 거래 내역 조회 */
    List<Transaction> findByBuyerId(Long buyerId);

    /** 구매자 또는 판매자로 참여한 거래 내역 조회 (최신순) */
    @Query("SELECT t FROM Transaction t WHERE t.buyerId = :userId OR t.sellerId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    /** 거래 유형 필터 + 구매자/판매자 조회 (최신순) */
    @Query("SELECT t FROM Transaction t WHERE (t.buyerId = :userId OR t.sellerId = :userId) AND t.type = :type ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId,
        @Param("type") Transaction.TransactionType type);

    // 탈퇴 블록: 처리 중인 환불 TX 존재 여부 확인
    @Query("""
        SELECT COUNT(t) > 0
        FROM Transaction t
        WHERE t.buyerId = :userId
          AND t.type = com.ssafy.cheket.entity.transaction.Transaction$TransactionType.REFUND
          AND t.txStatus IN (
              com.ssafy.cheket.entity.transaction.Transaction$TxStatus.PENDING,
              com.ssafy.cheket.entity.transaction.Transaction$TxStatus.SUBMITTED
          )
        """)
    boolean existsPendingRefundByUserId(@Param("userId") Long userId);

}
