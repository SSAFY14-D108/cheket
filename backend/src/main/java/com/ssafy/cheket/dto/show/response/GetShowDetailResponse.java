package com.ssafy.cheket.dto.show.response;

import com.ssafy.cheket.enums.ShowStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GetShowDetailResponse(Long showId, String title, String posterUrl, String venue, Integer purchaseLimit,
    String region, ShowPeriod show, ReservationPeriod reservation, ShowStatus status, String description, String artist,
    Boolean isLiked, Integer likeCount, List<GradeInfo> grade, List<RefundPolicyInfo> refundPolicy) {

    public record ShowPeriod(LocalDate showStartDate, LocalDate showEndDate) {
    }

    public record ReservationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    }

    public record GradeInfo(Long sectionId, String gradeName, Integer price, String colorCode) {
    }

    public record RefundPolicyInfo(Integer daysRemaining, Integer refundRate) {
    }
}
