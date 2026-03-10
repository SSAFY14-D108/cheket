package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
