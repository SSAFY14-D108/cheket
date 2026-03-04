package com.example.cheketqr.domain.usecase

import com.example.cheketqr.domain.repository.TicketRepository
import com.example.cheketqr.domain.repository.VerifyTicketResult

class VerifyQrUseCase(
    private val repository: TicketRepository
) {
    suspend operator fun invoke(ticketId: Long, qrToken: String): VerifyTicketResult {
        return repository.verifyTicket(ticketId, qrToken)
    }
}
