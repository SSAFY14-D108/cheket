package com.ssafy.cheket.repository.resale.projection;

import java.time.LocalDateTime;

public interface ResaleTicketProjection {
    Long getTicketId();
    Long getSessionId();
    LocalDateTime getSessionDate();
    LocalDateTime getSessionStartTime();
    String getSectionName();
    Long getSeatId();
    String getSeatNo();
    String getGrade();
    Integer getOriginalPrice();
    Integer getDiscountedPrice();
    Double getDiscountRate();
}
