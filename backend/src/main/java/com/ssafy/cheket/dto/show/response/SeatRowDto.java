package com.ssafy.cheket.dto.show.response;

import com.ssafy.cheket.enums.SeatStatus;

public record SeatRowDto(Long sectionId, String sectionName, String gradeName, Integer price, String colorCode,
    Long sessionSeatId, Long seatId, Integer rowNum, Integer colNum, String seatNo, SeatStatus status) {
}
