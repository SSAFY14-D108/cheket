package com.ssafy.cheket.repository.show.projection;

import java.time.LocalDateTime;

public interface PurchaseSessionSeatProjection {

    String getShowTitle();

    LocalDateTime getSessionDate();

    String getVenueName();

    Long getSessionSeatId();

    String getSectionName();

    String getSeatNo();

    String getGrade();

    Integer getPrice();

    Integer getTotalPrice();

}
