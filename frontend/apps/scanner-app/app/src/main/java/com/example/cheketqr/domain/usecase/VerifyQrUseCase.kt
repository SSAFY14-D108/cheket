package com.example.cheketqr.domain.usecase

import com.example.cheketqr.domain.repository.CheckInResult
import com.example.cheketqr.domain.repository.TicketRepository

class VerifyQrUseCase(
    private val repository: TicketRepository,
) {
    suspend operator fun invoke(qrToken: String): CheckInResult {
        return repository.checkIn(qrToken)
    }
}
