package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    int countBySectionIdIn(List<Long> sectionIds);
}
