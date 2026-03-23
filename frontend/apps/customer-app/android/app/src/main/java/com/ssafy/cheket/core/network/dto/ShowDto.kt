package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

// ── Show List (paginated) ──

data class ShowListResponse(
    @SerializedName("shows") val shows: List<ShowSummaryDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
)

data class ShowSummaryDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("purchaseLimit") val purchaseLimit: Int? = null,
    @SerializedName("region") val region: String,
    @SerializedName("show") val show: ShowPeriodDto? = null,
    @SerializedName("reservation") val reservation: ReservationPeriodDto? = null,
    @SerializedName("status") val status: String,
)

data class ShowPeriodDto(
    @SerializedName("showStartDate") val showStartDate: String? = null,
    @SerializedName("showEndDate") val showEndDate: String? = null,
)

data class ReservationPeriodDto(
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
)

// ── Show Detail ──

data class ShowDetailDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("description") val description: String?,
    @SerializedName("artist") val artist: String?,
    @SerializedName("venue") val venue: String,
    @SerializedName("show") val show: ShowPeriodDto? = null,
    @SerializedName("reservation") val reservation: ReservationPeriodDto? = null,
    @SerializedName("region") val region: String,
    @SerializedName("status") val status: String,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("grade") val grade: List<GradeDto>,
    @SerializedName("refundPolicy") val refundPolicy: List<RefundPolicyDto>?,
    @SerializedName("purchaseLimit") val purchaseLimit: Int? = null,
    @SerializedName("descriptionImages") val descriptionImages: List<String>? = null,
)

data class GradeDto(
    @SerializedName("sectionId") val sectionId: Long,
    @SerializedName("gradeName") val gradeName: String,
    @SerializedName("price") val price: Int,
    @SerializedName("colorCode") val colorCode: String? = null,
)

data class RefundPolicyDto(
    @SerializedName("daysRemaining") val daysRemaining: Int,
    @SerializedName("refundRate") val refundRate: Int,
)

// ── Sessions ──

data class SessionDto(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("sessionDate") val sessionDate: String,
    @SerializedName("sessionStartTime") val sessionStartTime: String,
    @SerializedName("remainingSeats") val remainingSeats: Int,
    @SerializedName("totalSeats") val totalSeats: Int,
)

// ── Seats ──

/** Backend returns List<GetSeatsResponse> directly as data field */
typealias SeatsResponse = List<SeatSectionDto>

data class SeatSectionDto(
    @SerializedName("sectionId") val sectionId: Long,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("gradeName") val gradeName: String,
    @SerializedName("price") val price: Int,
    @SerializedName("colorCode") val colorCode: String,
    @SerializedName("seats") val seats: List<SeatDto>,
)

data class SeatDto(
    @SerializedName("sessionSeatId") val sessionSeatId: Long,
    @SerializedName("seatId") val seatId: Long,
    @SerializedName("rowNum") val rowNum: Int,
    @SerializedName("colNum") val colNum: Int,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("status") val status: String,
)

// ── Refund Policy ──

data class RefundPolicyResponse(
    @SerializedName("refundPolicy") val refundPolicy: List<RefundPolicyDto>,
    @SerializedName("showStartDate") val showStartDate: String,
)

// ── Upcoming Shows ──

data class UpcomingShowsResponse(
    @SerializedName("shows") val shows: List<ShowSummaryDto>,
)

// ── Recommendations (AI 추천) ──

data class RecommendationsResponse(
    @SerializedName("shows") val shows: List<RecommendedShowDto>,
)

data class RecommendedShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("purchaseLimit") val purchaseLimit: Int? = null,
    @SerializedName("region") val region: String,
    @SerializedName("show") val show: ShowPeriodDto? = null,
    @SerializedName("reservation") val reservation: ReservationPeriodDto? = null,
    @SerializedName("status") val status: String,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("ticketingState") val ticketingState: String? = null,
    @SerializedName("showState") val showState: String? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("reason") val reason: String? = null,
)

// ── Liked Shows — LikedShowDto는 UserDto.kt에 정의 ──
