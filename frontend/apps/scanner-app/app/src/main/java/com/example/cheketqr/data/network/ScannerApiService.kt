package com.example.cheketqr.data.network

import com.example.cheketqr.data.model.ApiEnvelope
import com.example.cheketqr.data.model.VerifyQrRequest
import com.example.cheketqr.data.model.VerifyTicketData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ScannerApiService {
    @POST("/api/v1/hosts/tickets/{ticketId}/verify")
    suspend fun verifyTicket(
        @Path("ticketId") ticketId: Long,
        @Body request: VerifyQrRequest
    ): Response<ApiEnvelope<VerifyTicketData>>
}
