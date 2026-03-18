package com.ssafy.cheket.dto.host.response;

import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.enums.StakeholderRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record GetHostShowDetailResponse(Long showId, String title, String posterUrl, VenueInfo venue, ShowPeriod show,
    ReservationPeriod reservation, String description, String artist, Integer playtime, Integer purchaseLimit,
    Integer likeCount, List<GradeInfo> grade, List<StakeholderInfo> stakeholders, List<RefundPolicyInfo> refundPolicy,
    List<SessionInfo> sessionInfo, ShowStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
    List<String> descriptionImages) {

    public record VenueInfo(Long venueId, String name, String address) {
    }

    public record ShowPeriod(LocalDate showStartDate, LocalDate showEndDate) {
    }

    public record ReservationPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    }

    public record GradeInfo(Long sectionId, String gradeName, Integer price, String colorCode, Long ticketEffectId,
        String ticketEffect) {
    }

    public record StakeholderInfo(StakeholderRole role, Long id, String name, String number, Integer shareBps) {
    }

    public record RefundPolicyInfo(Integer daysRemaining, Integer refundRate) {
    }

    public record SessionInfo(Long sessionId, LocalDate sessionDate, LocalTime sessionStartTime, Integer capacity) {
    }

}
