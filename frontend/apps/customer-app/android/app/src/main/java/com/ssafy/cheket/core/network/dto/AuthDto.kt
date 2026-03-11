package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class LoginRequest(
    val email: String,
    val password: String,
)

data class SignupRequest(
    val username: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
)

data class SmsSendRequest(val phoneNumber: String)

data class SmsVerifyRequest(
    val phoneNumber: String,
    val code: String,
)

data class PasswordResetRequest(val email: String)

data class PasswordChangeRequest(
    val phoneNumber: String,
    val code: String,
    val newPassword: String,
)

data class EmailFindRequest(
    val username: String,
    val phoneNumber: String,
)

data class EmailDuplicateRequest(val email: String)

// ── Responses ──

data class LoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)

data class SmsVerifyResponse(
    @SerializedName("verified") val verified: Boolean,
)

data class EmailFindResponse(
    @SerializedName("email") val email: String,
)

data class UserSearchResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: String,
)
