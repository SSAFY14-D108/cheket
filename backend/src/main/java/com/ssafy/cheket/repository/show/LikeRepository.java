package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.like.Like;
import com.ssafy.cheket.entity.like.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    int countByShowId(Long showId);

    boolean existsByUserIdAndShowId(Long userId, Long showId);
}
