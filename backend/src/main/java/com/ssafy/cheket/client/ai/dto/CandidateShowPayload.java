package com.ssafy.cheket.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CandidateShowPayload(
    @JsonProperty("showId")
    Long showId,
    @JsonProperty("artist")
    String artist,
    @JsonProperty("title")
    String title,
    @JsonProperty("venue")
    String venue,
    @JsonProperty("embeddingText")
    String embeddingText,
    @JsonProperty("ticketingState")
    String ticketingState,
    @JsonProperty("showState")
    String showState,
    @JsonProperty("showStartDate")
    String showStartDate
) {
}
