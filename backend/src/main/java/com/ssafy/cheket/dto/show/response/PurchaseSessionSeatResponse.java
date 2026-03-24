package com.ssafy.cheket.dto.show.response;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseSessionSeatResponse(LocalDateTime expiresAt, String showTitle, LocalDateTime sessionDate,
    String venueName, Seats seats, int totalPrice) {

    public record Seats(List<SessionSeatInfo> success, List<SessionSeatInfo> failure) {
    }

    public record SessionSeatInfo(Long sessionSeatId, String sectionName, String seatNo, String grade, int price) {
    }

}
