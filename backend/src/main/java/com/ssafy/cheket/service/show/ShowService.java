package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.Region;

public interface ShowService {
    GetShowListResponse getShowList(Region region, int page, int size);
}
