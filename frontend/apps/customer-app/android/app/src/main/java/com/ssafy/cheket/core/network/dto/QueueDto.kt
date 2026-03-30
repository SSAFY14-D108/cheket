package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

/**
 * POST /api/v1/shows/{showId}/sessions/{sessionId}/queue 응답
 * 대기열 진입 시 받는 정보
 */
data class QueueEntryResponse(
    @SerializedName("queueToken") val queueToken: String,
    @SerializedName("status") val status: String,               // WAITING, ACTIVE
    @SerializedName("position") val position: Long,
    @SerializedName("aheadCount") val aheadCount: Long,
    @SerializedName("estimatedWaitSeconds") val estimatedWaitSeconds: Long,
    @SerializedName("pollingIntervalSeconds") val pollingIntervalSeconds: Int,
    @SerializedName("admitExpiresAt") val admitExpiresAt: String? = null,
)

/**
 * GET /api/v1/shows/{showId}/sessions/{sessionId}/queue/status 응답
 * 대기열 폴링 시 받는 정보
 */
data class QueueInfoResponse(
    @SerializedName("status") val status: String,                  // WAITING, ACTIVE, EXPIRED, LEFT, COMPLETED
    @SerializedName("position") val position: Long?,
    @SerializedName("aheadCount") val aheadCount: Long?,
    @SerializedName("estimatedWaitSeconds") val estimatedWaitSeconds: Long?,
    @SerializedName("admitExpiresAt") val admitExpiresAt: String? = null,
)

/**
 * POST /api/v1/shows/{showId}/sessions/{sessionId}/queue/enter 응답
 * ACTIVE 상태에서 좌석 선택 진입 시 받는 토큰
 */
data class QueueSeatEnterResponse(
    @SerializedName("seatAccessToken") val seatAccessToken: String,
    @SerializedName("seatAccessExpiresAt") val seatAccessExpiresAt: String,
)

// 하위 호환 유지 (기존 코드 참조)
typealias QueueStatusResponse = QueueEntryResponse
typealias QueueEnterResponse = QueueEntryResponse
