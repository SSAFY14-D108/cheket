package com.ssafy.cheket.dto.auth.request;

public record UserSignupRequest(String username, String phoneNumber, String email, String password) {
}
