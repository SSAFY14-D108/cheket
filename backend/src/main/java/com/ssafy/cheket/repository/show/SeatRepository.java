package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    int countBySectionIdIn(List<Long> sectionIds);

    // 구역 ID 목록으로 해당 좌석 전부 조회 -> session_seats 초기화할 때 사용
    List<Seat> findBySectionIdIn(List<Long> sectionIds);

    // 특정 공연이 사용하는 공연장의 전체 좌석 수
    @Query("""
            select count(s)
            from Seat s
            where s.sectionId in (
                select sec.id
                from Section sec
                where sec.venueId = (
                    select sh.venue.id
                    from Show sh
                    where sh.id = :showId
                )
            )
        """)
    long countByShowId(@Param("showId") Long showId);
}
