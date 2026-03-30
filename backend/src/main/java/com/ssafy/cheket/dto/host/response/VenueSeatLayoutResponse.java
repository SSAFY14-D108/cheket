package com.ssafy.cheket.dto.host.response;

import java.util.List;

public record VenueSeatLayoutResponse(Long sectionId, String sectionName, List<VenueSeatItemResponse> seats) {
}
