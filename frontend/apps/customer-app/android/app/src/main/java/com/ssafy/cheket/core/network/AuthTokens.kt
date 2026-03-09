package com.ssafy.cheket.core.network

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val accessTokenExpiresAt: Long = 0L
)
