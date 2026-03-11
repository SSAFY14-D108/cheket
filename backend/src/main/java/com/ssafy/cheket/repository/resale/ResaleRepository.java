package com.ssafy.cheket.repository.resale;

import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.repository.resale.projection.ResaleShowProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResaleRepository extends JpaRepository<Ticket, Long> {

    @Query(value = """
        select
            sh.id as showId,
            sh.title as title,
            sh.showStartDate as showStartDate,
            sh.showEndDate as showEndDate,
            v.name as venue,
            cast(v.region as string) as region,
            sh.posterUrl as posterUrl,
            count(t.id) as ticketCount
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session sess on sess.id = ss.sessionId
        join Show sh on sh.id = sess.showId
        join sh.venue v
        where t.resaleStatus = com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.LISTED
          and (:region is null or v.region = :region)
          and (
              :keyword is null
              or lower(sh.title) like lower(concat('%', :keyword, '%'))
              or lower(sh.artist) like lower(concat('%', :keyword, '%'))
              or lower(v.name) like lower(concat('%', :keyword, '%'))
              )
        group by sh.id, sh.title, sh.showStartDate, sh.showEndDate, sh.reservationEndDate, v.name, v.region, sh.posterUrl
        order by sh.reservationEndDate asc, sh.id desc
        """, countQuery = """
        select count(distinct sh.id)
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session sess on sess.id = ss.sessionId
        join Show sh on sh.id = sess.showId
        join sh.venue v
        where t.resaleStatus = com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.LISTED
          and (:region is null or v.region = :region)
          and (
                :keyword is null
                or lower(sh.title) like lower(concat('%', :keyword, '%'))
                or lower(sh.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
              )
        """)
    Page<ResaleShowProjection> searchListedShowsOrderByDeadline(@Param("region") Region region,
        @Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
        select
            sh.id as showId,
            sh.title as title,
            sh.showStartDate as showStartDate,
            sh.showEndDate as showEndDate,
            v.name as venue,
            cast(v.region as string) as region,
            sh.posterUrl as posterUrl,
            count(t.id) as ticketCount
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session sess on sess.id = ss.sessionId
        join Show sh on sh.id = sess.showId
        join sh.venue v
        where t.resaleStatus = com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.LISTED
          and (:region is null or v.region = :region)
          and (
                :keyword is null
                or lower(sh.title) like lower(concat('%', :keyword, '%'))
                or lower(sh.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
              )
        group by sh.id, sh.title, sh.showStartDate, sh.showEndDate, sh.reservationEndDate, v.name, v.region, sh.posterUrl
        order by
            (
                select count(r.id)
                from Resale r
                join Ticket rt on rt.id = r.ticketId
                join SessionSeat rss on rss.id = rt.sessionSeatId
                join Session rsess on rsess.id = rss.sessionId
                where rsess.showId = sh.id
                  and r.createdAt >= :oneWeekAgo
            ) desc,
            (
                select count(allT.id)
                from Ticket allT
                join SessionSeat ass on ass.id = allT.sessionSeatId
                join Session asess on asess.id = ass.sessionId
                where asess.showId = sh.id
            ) desc,
            sh.id desc
        """, countQuery = """
        select count(distinct sh.id)
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session sess on sess.id = ss.sessionId
        join Show sh on sh.id = sess.showId
        join sh.venue v
        where t.resaleStatus = com.ssafy.cheket.entity.ticket.Ticket.ResaleStatus.LISTED
          and (:region is null or v.region = :region)
          and (
              :keyword is null
              or lower(sh.title) like lower(concat('%', :keyword, '%'))
              or lower(sh.artist) like lower(concat('%', :keyword, '%'))
              or lower(v.name) like lower(concat('%', :keyword, '%'))
              )
        """)
    Page<ResaleShowProjection> searchListedShowsOrderByPopular(@Param("region") Region region,
        @Param("keyword") String keyword, @Param("oneWeekAgo") java.time.LocalDateTime oneWeekAgo, Pageable pageable);
}
