package com.ssafy.cheket.entity.seatgrade;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "seat_grades", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"show_id", "section_id"})
})
public class SeatGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id", nullable = false)
    private Long showId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(name = "grade_name", nullable = false, length = 30)
    private String gradeName;

    @Column(name = "color_code", nullable = false, length = 10)
    private String colorCode;

    @Column(name = "ticket_effect_id", nullable = false)
    private Long ticketEffectId;
}
