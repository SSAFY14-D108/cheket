package com.ssafy.cheket.dto.show.response;

public record SeatItemResponse(Long sessionSeatId, Long seatId, Integer rowNum, Integer colNum, String seatNo,
    String status) {
}
