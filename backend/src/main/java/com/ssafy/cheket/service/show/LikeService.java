package com.ssafy.cheket.service.show;

public interface LikeService {
    void addLike(Long userId, Long showId);

    void removeLike(Long userId, Long showId);
}
