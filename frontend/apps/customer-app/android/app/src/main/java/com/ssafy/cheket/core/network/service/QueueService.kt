package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface QueueService {

    @POST("api/v1/shows/{showId}/sessions/{sessionId}/queue")
    suspend fun enterQueue(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<QueueEntryResponse>

    @GET("api/v1/shows/{showId}/sessions/{sessionId}/queue/status")
    suspend fun getQueueStatus(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<QueueStatusResponse>

    @DELETE("api/v1/shows/{showId}/sessions/{sessionId}/queue")
    suspend fun leaveQueue(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<Unit>

    @POST("api/v1/shows/{showId}/sessions/{sessionId}/queue/enter")
    suspend fun enterSeatSelection(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<QueueEnterResponse>
}
