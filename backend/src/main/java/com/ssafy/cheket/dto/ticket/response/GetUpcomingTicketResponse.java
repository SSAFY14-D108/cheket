package com.ssafy.cheket.dto.ticket.response;

import com.ssafy.cheket.enums.ResaleStatus;

import java.time.LocalDateTime;

public record GetUpcomingTicketResponse(Long ticketId, String numbering, String posterUrl, ShowInfo showInfo, int price,
    Long seatId, String sectionName, String seatNo, String grade, ResaleStatus status) {

    public record ShowInfo(Long showId, String name, LocalDateTime date, String venue) {
    }

}
