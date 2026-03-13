package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.dto.ticket.response.GetUsedAndExpiredTicketResponse;

import java.util.List;

public interface TicketService {

    // 티켓 환불
    void refundTicket(Long ticketId);

    // 볼 예정인 티켓 조회
    List<GetUpcomingTicketResponse> getUpcomingTickets(Long id);

    // 관람 완료 / 만료 티켓 조회
    List<GetUsedAndExpiredTicketResponse> getUsedAndExpiredTickets(Long id);

}
