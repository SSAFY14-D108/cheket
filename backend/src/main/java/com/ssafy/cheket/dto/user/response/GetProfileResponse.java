package com.ssafy.cheket.dto.user.response;

public record GetProfileResponse(Long userId, String username, String phoneNumber, String email) {
}
