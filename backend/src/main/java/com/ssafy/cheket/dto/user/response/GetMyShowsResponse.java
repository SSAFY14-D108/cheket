package com.ssafy.cheket.dto.user.response;

import java.time.LocalDateTime;

public record GetMyShowsResponse(Long showId, String title, String artist, String posterUrl,
    LocalDateTime showStartDate, LocalDateTime showEndDate) {
}
