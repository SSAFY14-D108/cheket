package com.ssafy.cheket.dto.show.response;

import java.util.List;

public record GetRecommendationsResponse(List<RecommendedShowItem> shows) {
}
