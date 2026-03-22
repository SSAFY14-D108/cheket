package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.dto.ticket.response.GetUsedAndExpiredTicketResponse;

import java.util.List;

public interface TicketService {

    // 티켓 구매
    Long purchaseTickets(Long userId, Long showId, Long sessionId, String seatAccessToken, List<Long> sessionSeatIds);

    // 티켓 환불
    void refundTicket(Long ticketId);

    // 볼 예정인 티켓 조회
    List<GetUpcomingTicketResponse> getUpcomingTickets(Long id);

    // 관람 완료 / 만료 티켓 조회
    List<GetUsedAndExpiredTicketResponse> getUsedAndExpiredTickets(Long id);

    // 티켓 양도
    Long transferTicket(Long senderUserId, Long ticketId, String receiverPhoneNumber);

}
