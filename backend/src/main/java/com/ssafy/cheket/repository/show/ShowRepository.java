package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
        where (:region is null or v.region = :region)
          and (
                :keyword is null
                or s.title like concat('%', :keyword, '%')
                or s.artist like concat('%', :keyword, '%')
                or v.name like concat('%', :keyword, '%')
          )
        """)
    Page<Show> search(@Param("region") Region region, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
        select s
        from Show s
        WHERE s.status = com.ssafy.cheket.enums.ShowStatus.DRAFT
        order by
            (select COUNT(l) from Like l where l.showId = s.id) desc,
            s.reservationStartDate asc
        """)
    List<Show> findUpcomingTop5ByLikeCount(Pageable pageable);

    @EntityGraph(attributePaths = {"venue"})
    @Query(value = """
        select s
        from Show s
        join s.venue v
        left join Session sess on sess.showId = s.id
        left join SessionSeat ss on ss.sessionId = sess.id
        left join Ticket t on t.sessionSeatId = ss.id
        where (:region is null or v.region = :region)
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
        group by s
        order by count(t.id) desc, s.id desc
        """, countQuery = """
        select count(s)
        from Show s
        join s.venue v
        where (:region is null or v.region = :region)
          and (
                :keyword is null
                or lower(s.title) like lower(concat('%', :keyword, '%'))
                or lower(s.artist) like lower(concat('%', :keyword, '%'))
                or lower(v.name) like lower(concat('%', :keyword, '%'))
          )
        """)
    Page<Show> searchOrderByPopular(@Param("region") Region region, @Param("keyword") String keyword,
        Pageable pageable);

    boolean existsByHost_IdAndStatusNotAndShowEndDateAfter(Long hostId, ShowStatus status, LocalDateTime now);

}
