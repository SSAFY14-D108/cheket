package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.dto.show.response.SeatRowDto;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.repository.show.projection.HeldSeatLockProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionSeatRepository extends JpaRepository<SessionSeat, Long> {

    // 공연/회차별 좌석 배치도 및 등급 정보 조회
    @Query("""
        select new com.ssafy.cheket.dto.show.response.SeatRowDto(
            sec.id,
            sec.sectionName,
            sg.gradeName,
            sg.price,
            sg.colorCode,
            ss.id,
            seat.id,
            seat.rowNum,
            seat.colNum,
            seat.seatNo,
            ss.status
        )
        from SessionSeat ss
        join Seat seat on ss.seatId = seat.id
        join Section sec on seat.sectionId = sec.id
        join SeatGrade sg on sg.showId = :showId and sg.sectionId = sec.id
        where ss.sessionId = :sessionId
        order by sec.id, seat.rowNum, seat.colNum
        """)
    List<SeatRowDto> findSeatRowsByShowIdAndSessionId(Long showId, Long sessionId);

    // 세션별 전체 좌석 수 조회
    @Query("""
        select ss.sessionId, count(ss)
        from SessionSeat ss
        where ss.sessionId in :sessionIds
        group by ss.sessionId
        """)
    List<Object[]> countGroupedBySessionIds(List<Long> sessionIds);

    // 공연별 전체 좌석 수 조회
    @Query("""
        select count(ss.id)
        from SessionSeat ss
        join Session s on ss.sessionId = s.id
        where s.showId = :showId
        """)
    int countTotalSeatsByShowId(Long showId);

    // 특정 회차의 예매 완료 좌석 수
    @Query("""
            select count(ss)
            from SessionSeat ss
            where ss.sessionId = :sessionId
              and ss.status = com.ssafy.cheket.enums.SeatStatus.SOLD
        """)
    int countReservedSeatsBySessionId(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("DELETE FROM SessionSeat ss WHERE ss.sessionId IN :sessionIds")
    void deleteBySessionIdIn(@Param("sessionIds") List<Long> sessionIds);

    // 특정 회차의 모든 좌석 조회 (배치 민팅 시 사용)
    List<SessionSeat> findBySessionId(Long sessionId);

    // PENDING_TX 좀비 좌석 복원: threshold 이전에 PENDING_TX로 고착된 좌석 조회
    // Ticket 존재 여부로 복원 상태 판단 (EXISTS → 환불 중 고착 → SOLD, 없음 → 구매 중 고착 → AVAILABLE)
    @Query("""
        SELECT ss FROM SessionSeat ss
        WHERE ss.status = com.ssafy.cheket.enums.SeatStatus.PENDING_TX
          AND ss.updatedAt < :threshold
        """)
    List<SessionSeat> findStuckPendingTxSeats(@Param("threshold") LocalDateTime threshold);

    @Query(value = """
        select
            ss.id as sessionSeatId,
            ss.session_id as sessionId,
            s.show_id as showId
        from session_seats ss
        join sessions s on s.id = ss.session_id
        where ss.status = 'HELD'
        """, nativeQuery = true)
    List<HeldSeatLockProjection> findHeldSeatLockTargets();
}
