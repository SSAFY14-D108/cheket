package com.ssafy.cheket.dto.show.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionListResponse(Long sessionId,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate sessionDate,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss") LocalTime sessionStartTime,

    Integer remainingSeats, Integer totalSeats) {
}
