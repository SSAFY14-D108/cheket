package com.ssafy.cheket.repository.ticket.projection;

import com.ssafy.cheket.entity.ticket.Ticket;

import java.time.LocalDateTime;

public interface UpcomingTicketProjection {

    Long getTicketId();

    String getNumbering();

    String getPosterUrl();

    Long getShowId();

    String getShowName();

    LocalDateTime getSessionDate();

    String getVenueName();

    Integer getPrice();

    Long getSeatId();

    String getSectionName();

    String getSeatNo();

    String getGrade();

    Ticket.ResaleStatus getStatus();

}
