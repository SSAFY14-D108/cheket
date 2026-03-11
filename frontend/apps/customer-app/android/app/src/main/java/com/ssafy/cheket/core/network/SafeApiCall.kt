package com.ssafy.cheket.core.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

private const val TAG = "SafeApiCall"

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
        Log.e(TAG, "safeCall() HTTP error: $message (code=${e.code()})")
        Result.failure(Exception(message))
    } catch (e: Exception) {
        Log.e(TAG, "safeCall() exception: ${e.message}", e)
        Result.failure(e)
    }
}
