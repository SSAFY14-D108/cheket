package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

data class UserInfoResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("email") val email: String,
)

data class NotificationRequest(
    @SerializedName("notificationEnable") val notificationEnable: Boolean,
)

data class FcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String,
)

// ── Notification ──

data class NotificationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,        // SHOW_START, SETTLEMENT, APPROVED, REJECTED, RESALE, RQ_CREATE, RQ_UPDATE
    @SerializedName("read") val isRead: Boolean,
    @SerializedName("showId") val showId: Long? = null,
)

// ── Liked Shows ──

data class LikedShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("showStartDate") val showDate: String,
    @SerializedName("status") val status: String,
)
