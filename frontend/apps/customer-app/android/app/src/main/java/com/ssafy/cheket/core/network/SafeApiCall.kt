package com.ssafy.cheket.core.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

data class ApiError(
    @SerializedName("errorMessage") val errorMessage: String?,
    @SerializedName("httpStatusCode") val httpStatusCode: Int?
)

suspend fun <T> safeCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val message = try {
            Gson().fromJson(errorBody, ApiError::class.java)?.errorMessage
        } catch (_: Exception) {
            null
        } ?: "HTTP ${e.code()} error"
        Result.failure(Exception(message))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
