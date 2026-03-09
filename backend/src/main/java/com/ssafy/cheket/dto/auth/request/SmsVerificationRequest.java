package com.ssafy.cheket.dto.auth.request;

public record SmsVerificationRequest(
    String phoneNumber,
    String code
) {
}
