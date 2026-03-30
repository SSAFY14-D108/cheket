package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Like;
import com.ssafy.cheket.entity.show.LikeId;
import com.ssafy.cheket.entity.show.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    int countByShowId(Long showId);

    boolean existsByUserIdAndShowId(Long userId, Long showId);

    void deleteByUserIdAndShowId(Long userId, Long showId);

    @Query("""
        select s
        from Like l
        join Show s on s.id = l.showId
        where l.userId = :userId
        order by l.createdAt desc
        """)
    List<Show> findLikedShowsByUserId(@Param("userId") Long userId);
}
