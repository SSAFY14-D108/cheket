package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetShowListResponse;

public interface ShowService {
    GetShowListResponse getShowList(int page, int size);
}
