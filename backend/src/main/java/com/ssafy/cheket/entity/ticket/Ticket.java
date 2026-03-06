package com.ssafy.cheket.entity.ticket;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "tickets")
public class Ticket {

    public enum ResaleStatus {
        AVAILABLE, SOLD, LISTED, USED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_seat_id", nullable = false, unique = true)
    private Long sessionSeatId;

    @Column(nullable = false, length = 20)
    private String numbering;

    @Column(name = "ticket_nft_id")
    private Long ticketNftId;

    @Column(name = "metadata_ipfs_cid", length = 100)
    private String metadataIpfsCid;

    @Enumerated(EnumType.STRING)
    @Column(name = "resale_status", nullable = false)
    private ResaleStatus resaleStatus;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
