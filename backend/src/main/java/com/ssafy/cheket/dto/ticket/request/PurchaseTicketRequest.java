package com.ssafy.cheket.dto.ticket.request;

import java.util.List;

/**
 * 티켓 구매 요청 DTO
 *
 * @param sessionSeatIds
 *            구매할 좌석 ID 목록
 */
public record PurchaseTicketRequest(List<Long> sessionSeatIds) {
}
