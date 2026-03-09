package com.ssafy.cheket.dto.auth.request;

public record ResetPasswordRequest(String phoneNumber, String code, String newPassword) {
}
