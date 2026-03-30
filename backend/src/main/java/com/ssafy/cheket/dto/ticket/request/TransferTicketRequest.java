package com.ssafy.cheket.dto.ticket.request;

/**
 * 지정 양도 요청 DTO
 *
 * 왜 전화번호만? Custodial 구조라서 사용자는 지갑 주소를 모름 서버가 전화번호 → User → Wallet → 지갑 주소로 변환
 */
public record TransferTicketRequest(String phoneNumber // 받는 사람 전화번호 (예: "010-1234-5678")
) {
}
