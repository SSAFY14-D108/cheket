package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class PurchaseRequest(
    @SerializedName("sessionSeatId") val sessionSeatId: List<Long>,
)

data class SeatLockRequest(
    @SerializedName("seatId") val seatId: List<String>,
)

data class TransferRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
)

// ── Responses ──

data class PurchaseResponse(
    @SerializedName("txId") val txId: Long,
)

data class QrResponse(
    @SerializedName("qrData") val qrData: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
)

// ── Ticket List ──

data class UpcomingTicketDto(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("numbering") val numbering: Int,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("show") val show: TicketShowDto,
    @SerializedName("price") val price: Int,
    @SerializedName("seatId") val seatId: Long,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("status") val status: String,
)

data class TicketShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("venue") val venue: String,
)

// ── Collection ──

data class CollectionTicketDto(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("show") val show: TicketShowDto,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
)
