package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Override
    @EntityGraph(attributePaths = {"venue"})
    Page<Show> findAll(Pageable pageable);
}
