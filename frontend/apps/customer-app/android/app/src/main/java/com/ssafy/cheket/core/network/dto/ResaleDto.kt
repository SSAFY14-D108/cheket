package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class ResaleRegisterRequest(
    @SerializedName("resalePrice") val resalePrice: Int,
)

// ── Resale Show List (paginated) ──

data class ResaleShowListResponse(
    @SerializedName("shows") val shows: List<ResaleShowDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
)

data class ResaleShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("showStartDate") val showStartDate: String,
    @SerializedName("showEndDate") val showEndDate: String?,
    @SerializedName("venue") val venue: String,
    @SerializedName("region") val region: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("ticketCount") val ticketCount: Int,
)

// ── Resale Tickets for a Show (paginated) ──

data class ResaleTicketListResponse(
    @SerializedName("tickets") val tickets: List<ResaleTicketDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
)

data class ResaleTicketDto(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("session") val session: ResaleSessionDto,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatId") val seatId: Long,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("originalPrice") val originalPrice: Int,
    @SerializedName("discountedPrice") val discountedPrice: Int,
    @SerializedName("discountRate") val discountRate: Double,
)

data class ResaleSessionDto(
    @SerializedName("sessionId") val sessionId: Long,
    @SerializedName("sessionDate") val sessionDate: String,
    @SerializedName("sessionStartTime") val sessionStartTime: String,
)
