package com.ssafy.cheket.dto.resale.response;

import java.time.LocalDate;

public record ResaleShowItem(Long showId, String title, LocalDate showStartDate, LocalDate showEndDate, String venue,
    String region, String posterUrl, Long ticketCount) {
}
