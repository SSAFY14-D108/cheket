package com.ssafy.cheket.repository.ticket.projection;

import java.time.LocalDateTime;

public interface UsedAndExpiredTicketProjection {

    Long getTicketId();

    String getNumbering();

    String getPosterUrl();

    Long getShowId();

    String getShowName();

    LocalDateTime getSessionDate();

    String getVenueName();

    String getEffect();

    Long getSeatId();

    String getSectionName();

    String getSeatNo();

    String getGrade();

}
