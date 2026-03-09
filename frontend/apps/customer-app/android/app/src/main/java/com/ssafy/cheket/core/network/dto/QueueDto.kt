package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

data class QueueEntryResponse(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("position") val position: Int,
    @SerializedName("totalWaiting") val totalWaiting: Int,
    @SerializedName("estimatedWaitSec") val estimatedWaitSec: Int,
)

data class QueueStatusResponse(
    @SerializedName("position") val position: Int,
    @SerializedName("totalWaiting") val totalWaiting: Int,
    @SerializedName("estimatedWaitSec") val estimatedWaitSec: Int,
    @SerializedName("status") val status: String,
)

data class QueueEnterResponse(
    @SerializedName("sessionToken") val sessionToken: String,
    @SerializedName("remainingSec") val remainingSec: Int,
    @SerializedName("purchaseLimit") val purchaseLimit: Int,
)
