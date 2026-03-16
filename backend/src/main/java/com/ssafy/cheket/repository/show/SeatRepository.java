package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    int countBySectionIdIn(List<Long> sectionIds);

    // 구역 ID 목록으로 해당 좌석 전부 조회 -> session_seats 초기화할 때 사용
    List<Seat> findBySectionIdIn(List<Long> sectionIds);
}
