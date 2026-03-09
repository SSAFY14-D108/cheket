package com.ssafy.cheket.dto.show.response;

import com.ssafy.cheket.entity.show.SessionSeat;

public record SeatRowDto(Long sectionId, String sectionName, String gradeName, Integer price, String colorCode,
    Long sessionSeatId, Long seatId, Integer rowNum, Integer colNum, String seatNo, SessionSeat.SeatStatus status) {
}
