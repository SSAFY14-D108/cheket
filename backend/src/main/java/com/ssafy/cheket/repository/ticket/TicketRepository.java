package com.ssafy.cheket.repository.ticket;

import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.repository.ticket.projection.UpcomingTicketProjection;
import com.ssafy.cheket.repository.ticket.projection.UsedAndExpiredTicketProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // 볼 예정인 티켓 목록 조회
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
                  com.ssafy.cheket.enums.ResaleStatus.AVAILABLE,
                  com.ssafy.cheket.enums.ResaleStatus.LISTED
              )
              and s.sessionDate >= :now
            order by s.sessionDate asc, s.sessionStartTime asc, t.id asc
        """)
    List<UpcomingTicketProjection> findUpcomingAvailableAndListedTicketsByUserId(@Param("userId") Long userId,
        @Param("now") LocalDateTime now);

    // 티켓 컬렉션 목록 조회
    @Query("""
            select
                t.id as ticketId,
                t.numbering as numbering,
                sh.posterUrl as posterUrl,
                sh.id as showId,
                sh.title as showName,
                s.sessionDate as sessionDate,
                v.name as venueName,
                te.effect as effect,
                seat.id as seatId,
                sec.sectionName as sectionName,
                seat.seatNo as seatNo,
                sg.gradeName as grade
            from Ticket t
            join SessionSeat ss on ss.id = t.sessionSeatId
            join Session s on s.id = ss.sessionId
            join Show sh on sh.id = s.showId
            join sh.venue v
            join Seat seat on seat.id = ss.seatId
            join Section sec on sec.id = seat.sectionId
            join SeatGrade sg on sg.showId = sh.id and sg.sectionId = sec.id
            left join TicketEffect te on te.id = sg.ticketEffectId
            where t.userId = :userId
              and t.resaleStatus in (
                  com.ssafy.cheket.enums.ResaleStatus.USED,
                  com.ssafy.cheket.enums.ResaleStatus.EXPIRED
              )
            order by s.sessionDate desc, s.sessionStartTime desc, t.id desc
        """)
    List<UsedAndExpiredTicketProjection> findUsedAndExpiredTicketsByUserId(@Param("userId") Long userId);

    // 총 판매 금액 조회
    @Query("""
        select coalesce(sum(sg.price), 0)
        from Ticket t
        join SessionSeat ss on t.sessionSeatId = ss.id
        join Session s on ss.sessionId = s.id
        join Seat seat on ss.seatId = seat.id
        join SeatGrade sg on sg.showId = s.showId and sg.sectionId = seat.sectionId
        where s.showId = :showId
        """)
    Integer sumPrimarySalesByShowId(Long showId);

    // 공연별 예약된 좌석 수 조회
    @Query("""
        select count(t.id)
        from Ticket t
        join SessionSeat ss on t.sessionSeatId = ss.id
        join Session s on ss.sessionId = s.id
        where s.showId = :showId
        """)
    int countReservedSeatsByShowId(Long showId);
}
