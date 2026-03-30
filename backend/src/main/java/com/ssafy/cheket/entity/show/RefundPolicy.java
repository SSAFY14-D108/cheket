package com.ssafy.cheket.entity.show;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "refund_policies")
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "days_remaining", nullable = false)
    private Integer daysRemaining;

    @Column(name = "refund_rate", nullable = false)
    private Integer refundRate;
}
