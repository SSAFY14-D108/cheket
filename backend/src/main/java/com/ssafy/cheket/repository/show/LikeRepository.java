package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.Like;
import com.ssafy.cheket.entity.show.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    int countByShowId(Long showId);

    boolean existsByUserIdAndShowId(Long userId, Long showId);

    void deleteByUserIdAndShowId(Long userId, Long showId);
}
