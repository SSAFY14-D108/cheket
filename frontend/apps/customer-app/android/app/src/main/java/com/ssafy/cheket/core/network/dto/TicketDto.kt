package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

// ── Requests ──

data class PurchaseRequest(
    @SerializedName("sessionSeatIds") val sessionSeatIds: List<Long>,
)

data class SeatLockRequest(
    @SerializedName("sessionSeatIds") val sessionSeatIds: List<Long>,
)

// ── Seat Lock Response ──

data class SeatLockResponse(
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("showTitle") val showTitle: String,
    @SerializedName("sessionDate") val sessionDate: String,
    @SerializedName("venueName") val venueName: String,
    @SerializedName("seats") val seats: SeatLockSeats,
    @SerializedName("totalPrice") val totalPrice: Int,
)

data class SeatLockSeats(
    @SerializedName("success") val success: List<SeatLockSeatItem>,
    @SerializedName("failure") val failure: List<SeatLockSeatItem>,
)

data class SeatLockSeatItem(
    @SerializedName("sessionSeatId") val sessionSeatId: Long,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("price") val price: Int,
)

data class TransferRequest(
    @SerializedName("phoneNumber") val phoneNumber: String,
)

// ── Responses ──

data class PurchaseResponse(
    @SerializedName("txId") val txId: Long,
)

data class QrTokenResponse(
    @SerializedName("qrToken") val qrToken: String,
    @SerializedName("expiresIn") val expiresIn: Int, // 만료까지 남은 초
)

// ── Ticket List ──

data class UpcomingTicketDto(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("numbering") val numbering: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("showInfo") val show: TicketShowDto,
    @SerializedName("price") val price: Int,
    @SerializedName("seatId") val seatId: Long,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("status") val status: String,
    @SerializedName("resalePrice") val resalePrice: Int? = null,
    @SerializedName("posterIpfsCid") val posterIpfsCid: String? = null,
    @SerializedName("metadataIpfsCid") val metadataIpfsCid: String? = null,
)

data class TicketShowDto(
    @SerializedName("showId") val showId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("venue") val venue: String,
    @SerializedName("effect") val effect: String? = null,
)

// ── Collection (onchain) ──

data class CollectionOnchainDto(
    @SerializedName("ticketId") val ticketId: Long,
    @SerializedName("numbering") val numbering: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("show") val show: TicketShowDto,
    @SerializedName("seatId") val seatId: Long? = null,
    @SerializedName("sectionName") val sectionName: String,
    @SerializedName("seatNo") val seatNo: String,
    @SerializedName("grade") val grade: String,
    @SerializedName("onchain") val onchain: OnchainInfoDto? = null,
)

data class OnchainInfoDto(
    @SerializedName("tokenId") val tokenId: Long,
    @SerializedName("ownerAddress") val ownerAddress: String,
    @SerializedName("tokenURI") val tokenURI: String,
    @SerializedName("onchainStatus") val onchainStatus: String,
    @SerializedName("price") val price: Long,
    @SerializedName("mintedAt") val mintedAt: Long,
)
