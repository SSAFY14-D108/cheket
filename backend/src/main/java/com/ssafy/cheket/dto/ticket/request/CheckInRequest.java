package com.ssafy.cheket.dto.ticket.request;

// QR 검증 앱이 스캔한 QR 토큰을 서버에 전달
public record CheckInRequest(String qrToken // QR 코드에서 읽은 JWT 문자열 그대로
) {
}
