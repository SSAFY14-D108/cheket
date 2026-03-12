package com.ssafy.cheket.entity.transaction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 블록체인 트랜잭션 엔티티 — 모든 SSF 거래 내역을 DB에 기록
 *
 * [역할] 블록체인 트랜잭션은 체인에만 기록되면 조회가 느리고 불편하다. 그래서 우리 DB에도 거래 내역을 저장해서, API로 빠르게 조회할
 * 수 있게 한다.
 *
 * [생명주기] PENDING(생성) → SUBMITTED(블록체인 제출) → CONFIRMED(블록 확정) 또는 FAILED(실패)
 *
 * [판매자 다형성] seller_id는 거래 유형에 따라 다른 테이블을 참조한다: - PURCHASE/REFUND → hosts 테이블
 * (공연 주최자가 판매자) - RESALE → users 테이블 (재판매자가 판매자) - CHARGE/TRANSFER → null (플랫폼이
 * 제공, 특정 판매자 없음) seller_type ENUM으로 어느 테이블인지 명시한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "transaction")
public class Transaction {

    // ===== 거래 유형 (6가지) =====
    // CHARGE: SSF 충전 (실물화폐 → SSF)
    // PURCHASE: 1차 티켓 구매 (유저 → 호스트)
    // REFUND: 1차 구매 환불 (호스트 → 유저, 1차 구매만 가능)
    // RESALE_PURCHASE: 재판매 티켓 구매 (구매자 → 판매자)
    // RESALE_INCOME: 재판매 수익 정산 (판매자에게 SSF 입금)
    // TRANSFER: 초기 SSF 전송 (플랫폼 → 신규 유저/호스트)
    public enum TransactionType {
        CHARGE, PURCHASE, RESALE_INCOME, RESALE_PURCHASE, REFUND, TRANSFER
    }

    // ===== 트랜잭션 상태 (블록체인 생명주기) =====
    // PENDING: DB에 생성됨, 아직 블록체인에 전송 안 함 (txHash = null)
    // SUBMITTED: 블록체인에 tx 전송 완료 (txHash 있음, 블록 포함 대기중)
    // CONFIRMED: 블록에 포함 확정됨 (blockNumber 기록됨)
    // FAILED: 전송 실패 (RPC 에러, 잔액 부족, 가스 부족 등)
    public enum TxStatus {
        PENDING, SUBMITTED, CONFIRMED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 거래 유형 — CHARGE, PURCHASE, REFUND, RESALE_PURCHASE, RESALE_INCOME, TRANSFER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // 거래 금액 (SSF 단위)
    @Column(nullable = false)
    private Long amount;

    // 거래 설명 (예: "초기 SSF 전송: platform → 0x1234...")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // 블록체인 트랜잭션 해시
    // PENDING 상태에서는 null (아직 블록체인에 전송 안 했으므로)
    // SUBMITTED 이후부터 값이 채워짐 (예: "0xabc123...")
    // 이벤트 리스너가 이 해시로 DB 레코드를 찾아서 CONFIRMED로 업데이트
    @Column(name = "tx_hash", nullable = true, length = 100)
    private String txHash;

    // 트랜잭션 상태 — PENDING → SUBMITTED → CONFIRMED / FAILED
    @Enumerated(EnumType.STRING)
    @Column(name = "tx_status", nullable = false)
    private TxStatus txStatus;

    // 트랜잭션이 포함된 블록 번호
    // CONFIRMED 상태가 될 때 이벤트 리스너가 기록
    // 블록 번호로 "언제 확정됐는지" 추적 가능
    @Column(name = "block_number")
    private Long blockNumber;

    // 구매자 ID — 항상 users 테이블 참조 (FK 유지)
    // 호스트는 구매 행위를 하지 않으므로 buyer는 항상 일반 유저
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    // 판매자 ID — users 테이블만 참조 (2차 거래는 user만 가능)
    // RESALE → users(id), CHARGE/TRANSFER → null (플랫폼)
    // PURCHASE/REFUND → null (에스크로, host 정산은 settlements 테이블)
    @Column(name = "seller_id")
    private Long sellerId;

    // 레코드 생성 시각 — DB INSERT 시 자동 설정
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
