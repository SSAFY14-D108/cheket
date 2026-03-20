package com.ssafy.cheket.repository.ticket.projection;

import com.ssafy.cheket.enums.ResaleStatus;

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

    ResaleStatus getStatus();

    String getMetadataIpfsCid();

    int getResalePrice();

}
