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
)

data class GradeDto(
    @SerializedName("gradeId") val gradeId: Long,
    @SerializedName("gradeName") val gradeName: String,
    @SerializedName("price") val price: Int,
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

data class SeatsResponse(
    @SerializedName("sections") val sections: List<SeatSectionDto>,
)

data class SeatSectionDto(
    @SerializedName("sectionId") val sectionId: Long,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seats") val seats: List<SeatDto>,
)

data class SeatDto(
    @SerializedName("seatId") val seatId: Long,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("price") val price: Int,
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
