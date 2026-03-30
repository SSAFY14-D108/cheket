package com.ssafy.cheket.dto.host.response;

import java.time.LocalDate;
import java.util.List;

public record GetReservationsResponse(Long showId, String title, String venue, List<SessionInfo> sessions) {
    public record SessionInfo(Long sessionId, LocalDate date, Integer capacity, Integer reservedSeats) {
    }
}
