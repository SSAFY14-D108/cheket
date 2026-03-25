package com.ssafy.cheket.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecommendationRequestPayload(
    @JsonProperty("userId")
    Long userId,
    @JsonProperty("userEmbedding")
    List<Double> userEmbedding,
    @JsonProperty("userProfileText")
    String userProfileText,
    @JsonProperty("artistPreferences")
    List<ArtistPreferencePayload> artistPreferences,
    @JsonProperty("recentKeywords")
    List<String> recentKeywords,
    @JsonProperty("candidates")
    List<CandidateShowPayload> candidates
) {
}
