package com.ssafy.cheket.dto.show.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GetShowListResponse(List<ShowItem> shows, int page, int size, long totalElements, int totalPages) {
    public record ShowItem(Long showId, String title, String posterUrl, String venue, Integer purchaseLimit,
        String region, ShowPeriod show, ReservationPeriod reservation, String status) {
    }

    public record ShowPeriod(LocalDate showStartDate, LocalDate showEndDate) {
    }

    public record ReservationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    }
}
