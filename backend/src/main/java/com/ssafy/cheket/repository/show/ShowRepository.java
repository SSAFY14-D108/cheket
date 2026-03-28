package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.repository.show.projection.PurchaseSessionSeatProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @EntityGraph(attributePaths = {"venue"})
    @Query("""
        select s
        from Show s
        join s.venue v
        where (:regionCount = 0 or v.region.code in :regions)
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
          and s.status <> com.ssafy.cheket.enums.ShowStatus.PENDING_CONTRACT
          and (:includeEnded = true or s.showEndDate >= :now)
        """)
    Page<Show> search(@Param("regions") List<Integer> regions, @Param("regionCount") Long regionCount,
        @Param("keyword") String keyword, @Param("includeEnded") boolean includeEnded, @Param("now") LocalDateTime now,
        Pageable pageable);

    @Query("""
        select s
        from Show s
        WHERE s.status = com.ssafy.cheket.enums.ShowStatus.DRAFT
        order by
            (select COUNT(l) from Like l where l.showId = s.id) desc,
            s.reservationStartDate asc
        """)
    List<Show> findUpcomingTop5ByLikeCount(Pageable pageable);

    @EntityGraph(attributePaths = {"venue", "host", "venue.region"})
    @Query("""
        select s
        from Show s
        join s.venue v
        where s.status in (
                com.ssafy.cheket.enums.ShowStatus.DRAFT,
                com.ssafy.cheket.enums.ShowStatus.MINTING,
                com.ssafy.cheket.enums.ShowStatus.MINTED
              )
          and s.showEndDate >= :now
          and (
                :excludeLiked = false
                or not exists (
                    select 1
                    from Like l
                    where l.userId = :userId
                      and l.showId = s.id
                )
          )
        order by s.reservationStartDate asc, s.id desc
        """)
    List<Show> findRecommendationCandidates(@Param("userId") Long userId, @Param("excludeLiked") boolean excludeLiked,
        @Param("now") LocalDateTime now);

    // 인기순(예매수)
    @EntityGraph(attributePaths = {"venue", "venue.region"})
    @Query(value = """
        select s
        from Show s
        join s.venue v
        where (:regionCount = 0 or v.region.code in :regions)
          and s.status <> com.ssafy.cheket.enums.ShowStatus.PENDING_CONTRACT
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
          and (:includeEnded = true or s.showEndDate >= :now)
        order by s.reservationCount desc, s.id desc
        """)
    Page<Show> searchOrderByPopular(@Param("regions") List<Integer> regions, @Param("regionCount") Long regionCount,
        @Param("keyword") String keyword, @Param("includeEnded") boolean includeEnded, @Param("now") LocalDateTime now,
        Pageable pageable);

    // 마감 임박순
    @EntityGraph(attributePaths = {"venue"})
    @Query("""
        select s
        from Show s
        join s.venue v
        where (:regionCount = 0 or v.region.code in :regions)
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
          and s.reservationEndDate > :now
          and s.status <> com.ssafy.cheket.enums.ShowStatus.PENDING_CONTRACT
        """)
    Page<Show> searchOrderByDeadline(@Param("regions") List<Integer> regions, @Param("regionCount") Long regionCount,
        @Param("keyword") String keyword, @Param("now") LocalDateTime now, Pageable pageable);

    // 오픈 임박순
    @EntityGraph(attributePaths = {"venue"})
    @Query("""
        select s
        from Show s
        join s.venue v
        where (:regionCount = 0 or v.region.code in :regions)
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
          and s.reservationStartDate > :now
          and s.status <> com.ssafy.cheket.enums.ShowStatus.PENDING_CONTRACT
        """)
    Page<Show> searchOrderByOpenSoon(@Param("regions") List<Integer> regions, @Param("regionCount") Long regionCount,
        @Param("keyword") String keyword, @Param("now") LocalDateTime now, Pageable pageable);

    boolean existsByHost_IdAndStatusNotAndShowEndDateAfter(Long hostId, ShowStatus status, LocalDateTime now);

    // 예매 오픈 D-1 스케줄러: 내일 예매 시작이고 아직 DRAFT인 공연 조회
    List<Show> findByStatusAndReservationStartDateBetween(ShowStatus status, LocalDateTime from, LocalDateTime to);

    Page<Show> findByHost_id(Long hostId, Pageable pageable);

    @Query(value = """
        select
            sh.title as showTitle,
            sess.session_start_time as sessionDate,
            v.name as venueName,
            ss.id as sessionSeatId,
            sec.section_name as sectionName,
            seat.seat_no as seatNo,
            sg.grade_name as grade,
            sg.price as price,
            sum(sg.price) over () as totalPrice
        from session_seats ss
        join sessions sess on sess.id = ss.session_id
        join shows sh on sh.id = sess.show_id
        join venues v on v.id = sh.venue_id
        join seats seat on seat.id = ss.seat_id
        join sections sec on sec.id = seat.section_id
        join seat_grades sg on sg.show_id = sh.id and sg.section_id = sec.id
        where sh.id = :showId
          and sess.id = :sessionId
          and ss.id in :sessionSeatIds
        order by sec.id, seat.row_num, seat.col_num
        """, nativeQuery = true)
    List<PurchaseSessionSeatProjection> findPurchaseSessionSeats(@Param("showId") Long showId,
        @Param("sessionId") Long sessionId, @Param("sessionSeatIds") List<Long> sessionSeatIds);

    // 민팅 상태 원자적 변경 (DRAFT → MINTING, 중복 요청 방지)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Show s
        set s.status = com.ssafy.cheket.enums.ShowStatus.MINTING
        where s.id = :showId
          and s.status = com.ssafy.cheket.enums.ShowStatus.DRAFT
        """)
    int updateStatusToMintingIfDraft(@Param("showId") Long showId);

    // 예매수 증가
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Show s
        set s.reservationCount = s.reservationCount + :count
        where s.id = :showId
        """)
    int increaseReservationCount(@Param("showId") Long showId, @Param("count") int count);

    // 예매수 감소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Show s
        set s.reservationCount =
            case
                when s.reservationCount >= :count then s.reservationCount - :count
                else 0
            end
        where s.id = :showId
        """)
    int decreaseReservationCount(@Param("showId") Long showId, @Param("count") int count);

}
