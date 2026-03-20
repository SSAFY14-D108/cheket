package com.ssafy.cheket.dto.ticket.response;

/**
 * 티켓 구매 응답 DTO
 *
 * @param txId
 *            트랜잭션 ID (클라이언트가 상태 폴링에 사용)
 */
public record PurchaseTicketResponse(Long txId) {
}
