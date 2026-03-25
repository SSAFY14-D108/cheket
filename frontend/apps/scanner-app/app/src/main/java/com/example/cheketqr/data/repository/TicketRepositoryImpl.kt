package com.example.cheketqr.data.repository

import com.example.cheketqr.data.model.CheckInRequest
import com.example.cheketqr.data.network.ScannerApiService
import com.example.cheketqr.domain.repository.CheckInResult
import com.example.cheketqr.domain.repository.TicketRepository
import java.io.IOException

class TicketRepositoryImpl(
    private val apiService: ScannerApiService,
) : TicketRepository {

    override suspend fun checkIn(qrToken: String): CheckInResult {
        return try {
            val response = apiService.checkIn(CheckInRequest(qrToken))
            val body = response.body()

            if (response.isSuccessful && body?.data != null) {
                CheckInResult.Success(
                    message = body.responseMessage ?: "입장이 승인되었습니다.",
                    data = body.data,
                )
            } else {
                CheckInResult.Failure(
                    message = body?.errorMessage ?: "체크인에 실패했습니다. (${response.code()})",
                    statusCode = body?.httpStatusCode ?: response.code(),
                )
            }
        } catch (_: IOException) {
            CheckInResult.Failure("네트워크 연결을 확인해주세요.")
        } catch (e: Exception) {
            CheckInResult.Failure(e.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }
}
