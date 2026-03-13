package com.ssafy.cheket.dto.ticket.response;

import java.time.LocalDateTime;

public record GetUsedAndExpiredTicketResponse(Long ticketId, String numbering, String posterUrl, ShowInfo show,
    Long seatId, String sectionName, String seatNo, String grade) {

    public record ShowInfo(Long showId, String name, LocalDateTime date, String venue, String effect) {
    }
}
