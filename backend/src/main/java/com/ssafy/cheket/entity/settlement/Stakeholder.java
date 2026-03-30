package com.ssafy.cheket.entity.settlement;

import com.ssafy.cheket.enums.ApprovalStatus;
import com.ssafy.cheket.enums.StakeholderRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "stakeholder")
public class Stakeholder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "host_id")
    private Long hostId;

    @Column(name = "stakeholder_nft_id")
    private Long stakeholderNftId;

    @Column(name = "tx_hash", length = 100)
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StakeholderRole role;

    @Column(name = "share_bps", nullable = false)
    private Integer shareBps;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

}
