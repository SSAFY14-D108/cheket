package com.ssafy.cheket.entity.stakeholder;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "stakeholder_nft_id", nullable = false)
    private Long stakeholderNftId;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "share_bps", nullable = false)
    private Integer shareBps;
}
