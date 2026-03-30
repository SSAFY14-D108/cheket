package com.ssafy.cheket.repository.show.projection;

import java.time.LocalDateTime;

public interface SessionListProjection {

    Long getSessionId();

    LocalDateTime getSessionDate();

    LocalDateTime getSessionStartTime();

    Integer getRemainingSeats();

    Integer getTotalSeats();
}
