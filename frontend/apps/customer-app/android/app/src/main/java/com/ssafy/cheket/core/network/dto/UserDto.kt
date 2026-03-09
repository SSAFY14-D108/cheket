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

data class LikedShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("showDate") val showDate: String,
    @SerializedName("status") val status: String,
)
