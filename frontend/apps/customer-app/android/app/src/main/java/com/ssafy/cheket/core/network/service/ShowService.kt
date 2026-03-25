package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface ShowService {

    @GET("api/v1/shows")
    suspend fun getShows(
        @Query("regions") regions: List<Int>? = null,
        @Query("sort") sort: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<ShowListResponse>

    @GET("api/v1/shows/{showId}")
    suspend fun getShowDetail(@Path("showId") showId: Long): ApiResponse<ShowDetailDto>

    @GET("api/v1/shows/{showId}/sessions")
    suspend fun getSessions(@Path("showId") showId: Long): ApiResponse<List<SessionDto>>

    @GET("api/v1/shows/{showId}/sessions/{sessionId}/seats")
    suspend fun getSeats(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
    ): ApiResponse<SeatsResponse>

    @POST("api/v1/shows/{showId}/likes")
    suspend fun likeShow(@Path("showId") showId: Long): ApiResponse<Unit>

    @DELETE("api/v1/shows/{showId}/likes")
    suspend fun unlikeShow(@Path("showId") showId: Long): ApiResponse<Unit>

    @GET("api/v1/shows/{showId}/refund")
    suspend fun getRefundPolicy(@Path("showId") showId: Long): ApiResponse<RefundPolicyResponse>

    @GET("api/v1/shows/upcoming")
    suspend fun getUpcomingShows(): ApiResponse<UpcomingShowsResponse>

    /** AI 추천 공연 목록 (미구현 — 추후 연동) */
    @GET("api/v1/shows/recommendations")
    suspend fun getRecommendations(
        @Query("size") size: Int = 10,
        @Query("excludeLiked") excludeLiked: Boolean = false,
    ): ApiResponse<RecommendationsResponse>

    /** 찜한 공연 목록 */
    @GET("api/v1/users/likes")
    suspend fun getLikedShows(): ApiResponse<List<LikedShowDto>>

    /** 검색 키워드 저장 (AI 추천용) */
    @POST("api/v1/shows/search")
    suspend fun saveSearchKeyword(@Body request: SaveSearchKeywordRequest): ApiResponse<Unit>

    /** 계약 승인 */
    @POST("api/v1/shows/contracts/{showId}/approve")
    suspend fun approveContract(@Path("showId") showId: Long): ApiResponse<Unit>

    /** 계약 거절 */
    @POST("api/v1/shows/contracts/{showId}/reject")
    suspend fun rejectContract(@Path("showId") showId: Long): ApiResponse<Unit>
}
