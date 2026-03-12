package com.ssafy.cheket.repository.ticket;

import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.repository.ticket.projection.UpcomingTicketProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
        select
            t.id as ticketId,
            t.numbering as numbering,
            sh.posterUrl as posterUrl,
            sh.id as showId,
            sh.title as showName,
            s.sessionDate as sessionDate,
            v.name as venueName,
            sg.price as price,
            seat.id as seatId,
            sec.sectionName as sectionName,
            seat.seatNo as seatNo,
            sg.gradeName as grade,
            t.resaleStatus as status
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session s on s.id = ss.sessionId
        join Show sh on sh.id = s.showId
        join sh.venue v
        join Seat seat on seat.id = ss.seatId
        join Section sec on sec.id = seat.sectionId
        join SeatGrade sg on sg.showId = sh.id and sg.sectionId = sec.id
        where t.userId = :userId
          and t.resaleStatus in (
              com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.AVAILABLE,
              com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.LISTED
          )
          and s.sessionDate >= :now
        order by s.sessionDate asc, s.sessionStartTime asc, t.id asc
        """)
    List<UpcomingTicketProjection> findUpcomingAvailableAndListedTicketsByUserId(@Param("userId") Long userId,
        @Param("now") LocalDateTime now);

}
