package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface ResaleService {

    @POST("api/v1/resales/{ticketId}")
    suspend fun registerResale(
        @Path("ticketId") ticketId: Long,
        @Body request: ResaleRegisterRequest,
    ): ApiResponse<Unit>

    @GET("api/v1/resales")
    suspend fun getResaleShows(
        @Query("regions") regions: List<Int>? = null,
        @Query("sort") sort: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<ResaleShowListResponse>

    @GET("api/v1/resales/shows/{showId}")
    suspend fun getResaleTickets(
        @Path("showId") showId: Long,
        @Query("sort") sort: String? = null,
        @Query("sessionId") sessionId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<ResaleTicketListResponse>

    @HTTP(method = "POST", path = "api/v1/resales/{ticketId}/purchase")
    suspend fun purchaseResale(@Path("ticketId") ticketId: Long): ApiResponse<Unit>

    @DELETE("api/v1/resales/{ticketId}")
    suspend fun cancelResale(@Path("ticketId") ticketId: Long): ApiResponse<Unit>
}
