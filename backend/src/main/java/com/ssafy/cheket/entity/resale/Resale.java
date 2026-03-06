package com.ssafy.cheket.entity.resale;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "resales")
public class Resale {

    public enum ResaleListingStatus {
        ACTIVE, SOLD, CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;

    @Column(name = "resale_price", nullable = false)
    private Integer resalePrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResaleListingStatus status;

    @Column(name = "tx_hash", length = 100)
    private String txHash;

    @Column(name = "on_chain_listing_id")
    private Integer onChainListingId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
