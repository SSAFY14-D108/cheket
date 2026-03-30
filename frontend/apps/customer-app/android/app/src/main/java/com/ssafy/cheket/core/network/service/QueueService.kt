package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface QueueService {

    /** 대기열 진입 */
    @POST("api/v1/shows/{showId}/sessions/{sessionId}/queue")
    suspend fun enterQueue(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<QueueEntryResponse>

    /** 대기열 상태 폴링 (Queue-Token 헤더 필요) */
    @GET("api/v1/shows/{showId}/sessions/{sessionId}/queue/status")
    suspend fun getQueueStatus(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
        @Header("Queue-Token") queueToken: String,
    ): ApiResponse<QueueInfoResponse>

    /** 대기열 이탈 (Queue-Token 헤더 필요) */
    @DELETE("api/v1/shows/{showId}/sessions/{sessionId}/queue")
    suspend fun leaveQueue(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
        @Header("Queue-Token") queueToken: String,
    ): ApiResponse<Unit>

    /** 좌석 선택 진입 (ACTIVE 상태에서 호출, Queue-Token 헤더 필요) */
    @POST("api/v1/shows/{showId}/sessions/{sessionId}/queue/enter")
    suspend fun enterSeatSelection(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
        @Header("Queue-Token") queueToken: String,
    ): ApiResponse<QueueSeatEnterResponse>
}
