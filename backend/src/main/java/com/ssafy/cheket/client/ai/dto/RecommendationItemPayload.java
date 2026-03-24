package com.ssafy.cheket.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecommendationItemPayload(
    @JsonProperty("showId")
    Long showId,
    @JsonProperty("score")
    double score,
    @JsonProperty("reason")
    String reason
) {
}
