package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.request.PurchaseSessionSeatRequest;
import com.ssafy.cheket.dto.show.request.SaveSearchKeywordRequest;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.entity.show.ShowImage;
import com.ssafy.cheket.enums.ShowSort;

import java.util.List;

public interface ShowService {

    GetShowListResponse<ShowItem> getShowList(List<Integer> regions, ShowSort sort, String keyword,
        boolean includeEnded, int page, int size);

    GetShowDetailResponse getShowDetail(Long showId, Long userId);

    List<SessionListResponse> getSessionList(Long showId);

    List<GetSeatsResponse> getSeats(Long showId, Long sessionId);

    List<GetVenuesResponse> getVenues();

    GetRefundResponse getRefund(Long showId);

    GetUpcomingResponse getUpcoming();

    GetRecommendationsResponse getRecommendations(Long userId);

    List<GetSectionsResponse> getSections(Long venueId);

    // 공연 이미지 조회
    List<ShowImage> getShowImages(Long showId);

    // 공연 이미지 삭제
    void deleteShowImages(Long showId);

    // 좌석 선점(결제하기 버튼)
    PurchaseSessionSeatResponse purchaseSessionSeats(Long userId, Long showId, Long sessionId,
        PurchaseSessionSeatRequest request);

    void saveSearchKeyword(SaveSearchKeywordRequest request, Long userId);

}
