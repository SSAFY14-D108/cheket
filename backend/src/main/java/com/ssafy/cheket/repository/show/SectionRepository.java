package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByVenueId(Long venueId);

    List<Section> findByVenueIdOrderByIdAsc(Long id);
}
