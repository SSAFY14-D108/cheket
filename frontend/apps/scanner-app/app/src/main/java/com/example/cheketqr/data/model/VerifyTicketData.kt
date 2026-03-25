package com.example.cheketqr.data.model

import com.google.gson.annotations.SerializedName

data class CheckInRequest(
    @SerializedName("qrToken") val qrToken: String,
)

data class CheckInResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("ticketId") val ticketId: Long?,
    @SerializedName("userName") val userName: String?,
    @SerializedName("showTitle") val showTitle: String?,
    @SerializedName("sectionName") val sectionName: String?,
    @SerializedName("seatNo") val seatNo: String?,
    @SerializedName("grade") val grade: String?,
    @SerializedName("checkedInAt") val checkedInAt: String?,
)
