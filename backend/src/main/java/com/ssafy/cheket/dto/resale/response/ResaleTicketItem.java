package com.ssafy.cheket.dto.resale.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ResaleTicketItem(Long ticketId, SessionInfo session, String sectionName, Long seatId, String seatNo,
    String grade, Integer originalPrice, Integer discountedPrice, Double discountRate) {
    public record SessionInfo(Long sessionId, LocalDate sessionDate, LocalTime sessionStartTime) {
    }
}
