package com.ssafy.cheket.dto.user.request;

public record UserSignupRequest(String username, String phoneNumber, String email, String password) {
}
