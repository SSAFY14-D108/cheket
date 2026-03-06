package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @EntityGraph(attributePaths = {"venue"})
    @Query("select s from Show s")
    Page<Show> findAllWithVenue(Pageable pageable);

    @EntityGraph(attributePaths = {"venue"})
    Page<Show> findAllByVenueRegion(Region region, Pageable pageable);
}
