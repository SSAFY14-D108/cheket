package com.ssafy.cheket.client.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArtistPreferencePayload(
    @JsonProperty("artist")
    String artist,
    @JsonProperty("weight")
    double weight
) {
}
