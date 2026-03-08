package com.ssafy.cheket.dto.auth.request;

public record SmsSendVerificationRequest(
    String phoneNumber
) {
}
