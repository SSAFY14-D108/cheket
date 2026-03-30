package com.ssafy.cheket.dto.ticket.response;

public record QrTokenResponse(String qrToken, // QR 코드에 담길 JWT 문자열
    int expiresIn // 만료까지 남은 초 (프론트에서 카운트다운 표시용)
) {
}
