package com.ssafy.cheket.entity.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false, unique = true, length = 100)
    private String address;

    @Column(name = "ctk_balance")
    private Integer ctkBalance;

    @Column(name = "balance_synced_at")
    private LocalDateTime balanceSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "keystore_filename", nullable = false, length = 100)
    private String keystoreFilename;

    @PrePersist // save() 직전 자동 호출 -> createAt 자동 세팅
    protected void onCreated() {
        this.createdAt = LocalDateTime.now();
    }

}
