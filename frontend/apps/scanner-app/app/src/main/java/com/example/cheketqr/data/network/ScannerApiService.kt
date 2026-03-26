package com.example.cheketqr.data.network

import com.example.cheketqr.data.model.ApiEnvelope
import com.example.cheketqr.data.model.CheckInRequest
import com.example.cheketqr.data.model.CheckInResponse
import com.example.cheketqr.data.model.HostLoginRequest
import com.example.cheketqr.data.model.HostLoginResponse
import com.example.cheketqr.data.model.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ScannerApiService {
    @POST("/api/v1/hosts/auth/login")
    suspend fun hostLogin(
        @Body request: HostLoginRequest,
    ): Response<ApiEnvelope<HostLoginResponse>>

    @POST("/api/v1/hosts/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): Response<ApiEnvelope<HostLoginResponse>>

    @POST("/api/v1/tickets/check-in")
    suspend fun checkIn(
        @Body request: CheckInRequest,
    ): Response<ApiEnvelope<CheckInResponse>>
}
