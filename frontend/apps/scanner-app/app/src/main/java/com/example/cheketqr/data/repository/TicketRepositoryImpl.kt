package com.example.cheketqr.data.repository

import com.example.cheketqr.data.model.VerifyQrRequest
import com.example.cheketqr.data.network.ScannerApiService
import com.example.cheketqr.domain.repository.TicketRepository
import com.example.cheketqr.domain.repository.VerifyTicketResult
import java.io.IOException

class TicketRepositoryImpl(
    private val apiService: ScannerApiService
) : TicketRepository {

    override suspend fun verifyTicket(ticketId: Long, qrToken: String): VerifyTicketResult {
        return try {
            val response = apiService.verifyTicket(ticketId, VerifyQrRequest(qrToken))
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                VerifyTicketResult.Success(
                    message = body.responseMessage ?: "Entry approved.",
                    data = body.data
                )
            } else {
                VerifyTicketResult.Failure(
                    message = body?.errorMessage ?: "Verification failed. (${response.code()})",
                    statusCode = body?.httpStatusCode ?: response.code()
                )
            }
        } catch (_: IOException) {
            VerifyTicketResult.Failure("Please check your network connection.")
        } catch (e: Exception) {
            VerifyTicketResult.Failure(e.message ?: "Unknown error occurred.")
        }
    }
}
