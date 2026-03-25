package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface UserService {

    @GET("api/v1/users")
    suspend fun getUserInfo(): ApiResponse<UserInfoResponse>

    @DELETE("api/v1/users")
    suspend fun deleteUser(): ApiResponse<Unit>

    @GET("api/v1/users/likes")
    suspend fun getLikedShows(): ApiResponse<List<LikedShowDto>>

    @PUT("api/v1/users/notifications")
    suspend fun updateNotification(@Body request: NotificationRequest): ApiResponse<Unit>

    @GET("api/v1/users/notifications")
    suspend fun getNotifications(): ApiResponse<List<NotificationDto>>

    @PATCH("api/v1/users/notifications/{notificationId}")
    suspend fun markNotificationRead(@Path("notificationId") notificationId: Long): ApiResponse<Unit>

    @POST("api/v1/users/fcm-token")
    suspend fun saveFcmToken(@Body request: FcmTokenRequest): ApiResponse<Unit>
}
