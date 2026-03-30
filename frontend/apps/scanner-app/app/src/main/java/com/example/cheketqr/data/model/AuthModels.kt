package com.example.cheketqr.data.model

import com.google.gson.annotations.SerializedName

data class HostLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class HostLoginResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)
