package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface TicketService {

    @POST("api/v1/shows/{showId}/sessions/{sessionId}/purchase")
    suspend fun purchaseTicket(
        @Header("Seat-Access-Token") seatAccessToken: String,
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
        @Body request: PurchaseRequest,
    ): ApiResponse<PurchaseResponse>

    @POST("api/v1/shows/{showId}/sessions/{sessionId}/seats")
    suspend fun lockSeats(
        @Path("showId") showId: Long,
        @Path("sessionId") sessionId: Long,
        @Body request: SeatLockRequest,
    ): ApiResponse<Unit>

    @GET("api/v1/tickets/upcoming")
    suspend fun getUpcomingTickets(): ApiResponse<List<UpcomingTicketDto>>

    @GET("api/v1/tickets/collection")
    suspend fun getCollectionTickets(): ApiResponse<List<CollectionTicketDto>>

    @POST("api/v1/tickets/{ticketId}/transfer")
    suspend fun transferTicket(
        @Path("ticketId") ticketId: Long,
        @Body request: TransferRequest,
    ): ApiResponse<Unit>

    @POST("api/v1/tickets/{ticketId}/qr")
    suspend fun generateQr(@Path("ticketId") ticketId: Long): ApiResponse<QrResponse>

    @POST("api/v1/tickets/{ticketId}/refund")
    suspend fun refundTicket(@Path("ticketId") ticketId: Long): ApiResponse<Unit>

}
