package com.ssafy.cheket.repository.ticket;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.repository.ticket.projection.CheckInTicketProjection;
import com.ssafy.cheket.repository.ticket.projection.UpcomingTicketProjection;
import com.ssafy.cheket.repository.ticket.projection.UsedAndExpiredTicketProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                t.resaleStatus as status,
                t.metadataIpfsCid as metadataIpfsCid,
                rs.resalePrice as resalePrice
            from Ticket t
            join SessionSeat ss on ss.id = t.sessionSeatId
            join Session s on s.id = ss.sessionId
            join Show sh on sh.id = s.showId
            join sh.venue v
            join Seat seat on seat.id = ss.seatId
            join Section sec on sec.id = seat.sectionId
            join SeatGrade sg on sg.showId = sh.id and sg.sectionId = sec.id
            left join Resale rs on rs.ticketId = t.id
              and rs.status = com.ssafy.cheket.entity.resale.Resale.ResaleListingStatus.ACTIVE
            where t.userId = :userId
              and (
                  t.resaleStatus = com.ssafy.cheket.enums.ResaleStatus.AVAILABLE
                  or (
                      t.resaleStatus = com.ssafy.cheket.enums.ResaleStatus.LISTED
                      and rs.id is not null
                  )
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

    /**
     * 특정 사용자가 특정 공연의 티켓을 몇 장 갖고 있는지 양도 시 받는 사람의 maxPerWallet 초과 여부 확인용
     */
    @Query("SELECT COUNT(t) FROM Ticket t " + "JOIN SessionSeat ss ON t.sessionSeatId = ss.id "
        + "JOIN Session s ON ss.sessionId = s.id " + "WHERE t.userId = :userId AND s.showId = :showId")
    long countByUserIdAndShowId(@Param("userId") Long userId, @Param("showId") Long showId);

    // 특정 세션의 티켓을 보유한 사용자 아이디를 중복 없이 조회
    @Query("""
            select distinct t.userId
            from Ticket t
            join SessionSeat ss on ss.id = t.sessionSeatId
            where ss.sessionId = :sessionId
        """)
    List<Long> findDistinctUserIdsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 특정 사용자가 특정 회차의 티켓을 몇 장 갖고 있는지 구매 시 회차별 maxPerWallet 초과 여부 확인용 온체인
     * walletTicketCount[eventId][sessionId][wallet]와 동일 기준
     */
    @Query("SELECT COUNT(t) FROM Ticket t " + "JOIN SessionSeat ss ON t.sessionSeatId = ss.id "
        + "WHERE t.userId = :userId AND ss.sessionId = :sessionId")
    long countByUserIdAndSessionId(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    @Query("""
            select sh
            from Ticket t
            join SessionSeat ss on ss.id = t.sessionSeatId
            join Session s on s.id = ss.sessionId
            join Show sh on sh.id = s.showId
            where t.userId = :userId
            order by t.createdAt desc
        """)
    List<Show> findPurchasedShowsByUserId(@Param("userId") Long userId);

    // 체크인 시 좌석 + 공연 정보 조회
    @Query("""
        select
            t.id as ticketId,
            sh.title as showTitle,
            sec.sectionName as sectionName,
            seat.seatNo as seatNo,
            sg.gradeName as grade,
            v.name as venueName,
            s.sessionDate as sessionDate
        from Ticket t
        join SessionSeat ss on ss.id = t.sessionSeatId
        join Session s on s.id = ss.sessionId
        join Show sh on sh.id = s.showId
        join sh.venue v
        join Seat seat on seat.id = ss.seatId
        join Section sec on sec.id = seat.sectionId
        join SeatGrade sg on sg.showId = sh.id and sg.sectionId = sec.id
        where t.id = :ticketId
        """)
    CheckInTicketProjection findCheckInInfoByTicketId(@Param("ticketId") Long ticketId);

    // 온체인 NFT ID로 티켓 조회 - 온체인/DB 동기화 시 사용
    Optional<Ticket> findByTicketNftId(Long ticketNftId);

    // 탈퇴 블록: 리세일 등록 중인 티켓 존재 여부 확인
    boolean existsByUserIdAndResaleStatus(Long userId, com.ssafy.cheket.enums.ResaleStatus resaleStatus);

    // 좌석 복원: sessionSeatId로 Ticket 존재 여부 확인 (환불 중 고착 판별)
    boolean existsBySessionSeatId(Long sessionSeatId);
}
