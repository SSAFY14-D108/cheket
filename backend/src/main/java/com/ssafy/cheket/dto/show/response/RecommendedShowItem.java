package com.ssafy.cheket.dto.show.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecommendedShowItem(
    Long showId,
    String title,
    String posterUrl,
    String venue,
    Integer purchaseLimit,
    String region,
    ShowPeriod show,
    ReservationPeriod reservation,
    String status,
    String artist,
    String ticketingState,
    String showState,
    double score
) {
    public record ShowPeriod(LocalDate showStartDate, LocalDate showEndDate) {
    }

    public record ReservationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    }
}
