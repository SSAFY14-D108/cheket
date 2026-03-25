package com.example.cheketqr.domain.repository

import com.example.cheketqr.data.model.CheckInResponse

sealed interface CheckInResult {
    data class Success(
        val message: String,
        val data: CheckInResponse,
    ) : CheckInResult

    data class Failure(
        val message: String,
        val statusCode: Int? = null,
    ) : CheckInResult
}

interface TicketRepository {
    suspend fun checkIn(qrToken: String): CheckInResult
}
