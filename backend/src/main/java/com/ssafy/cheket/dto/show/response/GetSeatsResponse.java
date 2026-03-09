package com.ssafy.cheket.dto.show.response;

import java.util.List;

public record GetSeatsResponse(Long sectionId, String sectionName, String gradeName, Integer price, String colorCode,
    List<SeatItemResponse> seats) {
}
