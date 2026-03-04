package com.ssafy.cheket.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int httpStatusCode, String responseMessage, T data, String errorMessage) {

    // 성공
    public static <T> ApiResponse<T> ok(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data, null);
    }

    // 실패
    public static ApiResponse<Void> fail(int statusCode, String errorMessage) {
        return new ApiResponse<>(statusCode, null, null, errorMessage);
    }
}
