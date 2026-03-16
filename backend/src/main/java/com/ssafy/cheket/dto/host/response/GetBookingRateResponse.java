package com.ssafy.cheket.dto.host.response;

public record GetBookingRateResponse(Long showId, String title, Integer capacity, Integer reservedSeats,
    Double bookingRate) {
}
