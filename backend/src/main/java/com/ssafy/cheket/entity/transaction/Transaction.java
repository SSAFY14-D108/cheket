package com.ssafy.cheket.entity.transaction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "transaction")
public class Transaction {

    public enum TransactionType {
        CHARGE, PURCHASE, RESALE_INCOME, RESALE_PURCHASE, REFUND, TRANSFER
    }

    public enum TxStatus {
        PENDING, SUBMITTED, CONFIRMED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_status", nullable = false)
    private TxStatus txStatus;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
