package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;

import java.util.List;

public interface TicketService {

    // 티켓 환불
    void refundTicket(Long ticketId);

    // 볼 예정인 티켓 조회
    List<GetUpcomingTicketResponse> getUpcomingTickets(Long id);

}
