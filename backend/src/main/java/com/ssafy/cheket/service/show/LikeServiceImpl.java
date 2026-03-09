package com.ssafy.cheket.service.show;

import com.ssafy.cheket.entity.show.Like;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final ShowRepository showRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public void addLike(Long userId, Long showId) {
        if (!showRepository.existsById(showId))
            throw new NotFoundException("존재하지 않는 공연입니다.");

        if (likeRepository.existsByUserIdAndShowId(userId, showId))
            throw new ConflictException("이미 찜한 공연입니다.");

        Like like = Like.builder().userId(userId).showId(showId).build();

        likeRepository.save(like);
    }

    @Override
    @Transactional
    public void removeLike(Long userId, Long showId) {
        if (!showRepository.existsById(showId))
            throw new NotFoundException("존재하지 않는 공연입니다.");

        likeRepository.deleteByUserIdAndShowId(userId, showId);
    }
}
