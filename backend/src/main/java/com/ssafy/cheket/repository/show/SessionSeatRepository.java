package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.dto.show.response.SeatRowDto;
import com.ssafy.cheket.entity.show.SessionSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SessionSeatRepository extends JpaRepository<SessionSeat, Long> {

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
}
