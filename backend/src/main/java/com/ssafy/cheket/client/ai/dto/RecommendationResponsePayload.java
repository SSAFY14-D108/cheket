package com.ssafy.cheket.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecommendationResponsePayload(
    @JsonProperty("recommendations") List<RecommendationItemPayload> recommendations) {
}
