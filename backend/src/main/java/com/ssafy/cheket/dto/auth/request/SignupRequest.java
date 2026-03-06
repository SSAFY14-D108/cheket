package com.ssafy.cheket.dto.auth.request;

public record SignupRequest(
        String username,
        String phoneNumber,
        String email,
        String password
) {
}
