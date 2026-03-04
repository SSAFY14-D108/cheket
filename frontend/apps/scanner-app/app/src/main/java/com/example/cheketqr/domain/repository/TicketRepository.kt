package com.example.cheketqr.domain.repository

import com.example.cheketqr.data.model.VerifyTicketData

sealed interface VerifyTicketResult {
    data class Success(
        val message: String,
        val data: VerifyTicketData
    ) : VerifyTicketResult

    data class Failure(
        val message: String,
        val statusCode: Int? = null
    ) : VerifyTicketResult
}

interface TicketRepository {
    suspend fun verifyTicket(ticketId: Long, qrToken: String): VerifyTicketResult
}
