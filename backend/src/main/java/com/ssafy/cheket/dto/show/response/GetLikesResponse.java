package com.ssafy.cheket.dto.show.response;

import com.ssafy.cheket.enums.ShowStatus;

import java.time.LocalDate;

public record GetLikesResponse(Long showId, String title, String posterUrl, String venue, LocalDate showStartDate,
    ShowStatus status) {
}
