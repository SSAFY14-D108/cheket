package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetShowDetailResponse;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.SessionListResponse;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;

import java.util.List;

public interface ShowService {
    GetShowListResponse getShowList(Region region, ShowSort sort, String keyword, int page, int size);

    GetShowDetailResponse getShowDetail(Long showId);

    List<SessionListResponse> getSessionList(Long showId);
}
