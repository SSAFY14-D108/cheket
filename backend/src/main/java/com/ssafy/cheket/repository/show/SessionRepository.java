package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.repository.show.projection.SessionListProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Modifying
    @Query("DELETE FROM Session s WHERE s.showId = :showId")
    void deleteAllByShowId(@Param("showId") Long showId);

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

    boolean existsByIdAndShowId(Long sessionId, Long showId);

    List<Session> findByShowIdOrderBySessionDateAsc(Long showId);

    // 주어진 기간 [start, end)에 해당하는 세션 목록 조회
    List<Session> findBySessionDateGreaterThanEqualAndSessionDateLessThan(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 공연의 종료된 회차 조회 (onChainSessionId 있고, sessionStartTime + playtime < now)
     */
    @Query(value = """
        SELECT s.* FROM sessions s
        JOIN shows sh ON sh.id = s.show_id
        WHERE s.show_id = :showId
          AND s.on_chain_session_id IS NOT NULL
          AND TIMESTAMPADD(MINUTE, sh.playtime, s.session_start_time) < :now
        ORDER BY s.session_date ASC
        """, nativeQuery = true)
    List<Session> findCompletedSessionsByShowId(@Param("showId") Long showId, @Param("now") LocalDateTime now);

    /**
     * 전체 공연 중 종료된 회차 조회 (onChainSessionId 있고, sessionStartTime + playtime < now)
     */
    @Query(value = """
        SELECT s.* FROM sessions s
        JOIN shows sh ON sh.id = s.show_id
        WHERE s.on_chain_session_id IS NOT NULL
          AND sh.event_nft_id IS NOT NULL
          AND TIMESTAMPADD(MINUTE, sh.playtime, s.session_start_time) < :now
        ORDER BY s.session_date ASC
        """, nativeQuery = true)
    List<Session> findAllCompletedSessions(@Param("now") LocalDateTime now);
}
