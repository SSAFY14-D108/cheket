package com.ssafy.cheket.entity.sessionseat;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "session_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"session_id", "seat_id"})
})
public class SessionSeat {

    public enum SeatStatus {
        AVAILABLE, SOLD, HELD, PENDING_TX
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(name = "on_chain_ticket_nft_id")
    private Long onChainTicketNftId;
}
