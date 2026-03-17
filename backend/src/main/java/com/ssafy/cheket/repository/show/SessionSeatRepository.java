package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.dto.show.response.SeatRowDto;
import com.ssafy.cheket.entity.show.SessionSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
