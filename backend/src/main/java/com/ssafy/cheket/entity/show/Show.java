package com.ssafy.cheket.entity.show;

import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "poster_url", nullable = false, length = 200)
    private String posterUrl;

    @Column(name = "show_start_date", nullable = false)
    private LocalDateTime showStartDate;

    @Column(name = "show_end_date", nullable = false)
    private LocalDateTime showEndDate;

    @Column(name = "reservation_start_date", nullable = false)
    private LocalDateTime reservationStartDate;

    @Column(name = "reservation_end_date", nullable = false)
    private LocalDateTime reservationEndDate;

    @Column(name = "playtime", nullable = false)
    private Integer playtime;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "purchase_limit", nullable = false)
    private Integer purchaseLimit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "artist", nullable = false, length = 200)
    private String artist;

    @Column(name = "platform_fee_bps", nullable = false)
    private Integer platformFeeBps;

    @Column(name = "platform_wallet", nullable = false, length = 100)
    private String platformWallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShowStatus status;

    @Column(name = "poster_ipfs_cid", length = 100)
    private String posterIpfsCid;

    @Column(name = "metadata_ipfs_cid", length = 100)
    private String metadataIpfsCid;

    @Column(name = "event_nft_id")
    private Long eventNftId;
}
