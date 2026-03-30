package com.ssafy.cheket.repository.resale.projection;

import java.time.LocalDateTime;

public interface ResaleShowProjection {
    Long getShowId();
    String getTitle();
    LocalDateTime getShowStartDate();
    LocalDateTime getShowEndDate();
    String getVenue();
    String getRegion();
    String getPosterUrl();
    Long getTicketCount();
}
