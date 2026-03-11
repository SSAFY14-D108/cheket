package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;

import java.util.List;

public interface ShowService {
    GetShowListResponse<ShowItem> getShowList(Region region, ShowSort sort, String keyword, int page, int size);

    GetShowDetailResponse getShowDetail(Long showId);

    List<SessionListResponse> getSessionList(Long showId);

    List<GetSeatsResponse> getSeats(Long showId, Long sessionId);

    List<GetVenuesResponse> getVenues();

    GetRefundResponse getRefund(Long showId);

    GetUpcomingResponse getUpcoming();
}
