package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetLikesResponse;

import java.util.List;

public interface LikeService {
    void addLike(Long userId, Long showId);

    void removeLike(Long userId, Long showId);

    List<GetLikesResponse> getLikes(Long userId);
}
