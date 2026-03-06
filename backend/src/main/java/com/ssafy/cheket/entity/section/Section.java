package com.ssafy.cheket.entity.section;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_name", nullable = false, length = 50)
    private String sectionName;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

}
