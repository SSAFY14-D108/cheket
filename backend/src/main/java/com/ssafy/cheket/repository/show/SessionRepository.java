package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.repository.show.projection.SessionListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Query(value = """
        SELECT
            s.id AS sessionId,
            s.session_date AS sessionDate,
            s.session_start_time AS sessionStartTime,
            COALESCE(SUM(CASE WHEN ss.status = 'AVAILABLE' THEN 1 ELSE 0 END), 0) AS remainingSeats,
            COUNT(ss.id) AS totalSeats
        FROM sessions s
        LEFT JOIN session_seats ss ON ss.session_id = s.id
        WHERE s.show_id = :showId
        GROUP BY s.id, s.session_date, s.session_start_time
        ORDER BY s.session_date ASC, s.session_start_time ASC
        """, nativeQuery = true)
    List<SessionListProjection> findSessionListByShowId(@Param("showId") Long showId);
}
