package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.dto.ticket.response.CheckInResponse;
import com.ssafy.cheket.dto.ticket.response.QrTokenResponse;

public interface QrTokenService {

    // QR 토큰 발급 — 사용자가 본인 티켓에 대해 요청
    QrTokenResponse generateQrToken(Long userId, Long ticketId);

    // QR 토큰 검증 + DB 체크인 — 검증 앱(호스트)이 요청
    CheckInResponse verifyAndCheckIn(String qrToken);

    // QR 토큰 검증 + 온체인 체크인 — 검증 앱(호스트)이 요청
    CheckInResponse verifyAndCheckInOnchain(String qrToken);
}
