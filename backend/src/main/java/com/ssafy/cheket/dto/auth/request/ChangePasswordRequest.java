package com.ssafy.cheket.dto.auth.request;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
