package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface AuthService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @POST("api/v1/users")
    suspend fun signup(@Body request: SignupRequest): ApiResponse<Unit>

    @POST("api/v1/auth/sms/send")
    suspend fun sendSms(@Body request: SmsSendRequest): ApiResponse<Unit>

    @POST("api/v1/auth/sms/verify")
    suspend fun verifySms(@Body request: SmsVerifyRequest): ApiResponse<SmsVerifyResponse>

    @POST("api/v1/auth/password")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): ApiResponse<Unit>

    @PATCH("api/v1/auth/password")
    suspend fun resetPassword(@Body request: PasswordChangeRequest): ApiResponse<Unit>

    @POST("api/v1/auth/email")
    suspend fun findEmail(@Body request: EmailFindRequest): ApiResponse<EmailFindResponse>

    @GET("api/v1/auth/search")
    suspend fun searchUser(
        @Query("userType") userType: String,
        @Query("number") number: String,
    ): ApiResponse<UserSearchResponse>
}
