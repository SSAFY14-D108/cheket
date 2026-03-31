package com.example.cheketqr.data.network

import android.util.Log
import com.example.cheketqr.data.model.RefreshTokenRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.cheketqr.data.model.ApiEnvelope
import com.example.cheketqr.data.model.HostLoginResponse
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStore.accessToken
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .apply {
                if (token.isNotBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        val response = chain.proceed(request)

        // If 401 and we have a refresh token, try to refresh
        if (response.code == 401 && TokenStore.refreshToken.isNotBlank()) {
            Log.d(TAG, "Got 401, attempting token refresh")
            response.close()

            val refreshed = tryRefreshToken(chain)
            if (refreshed) {
                // Retry the original request with new token
                val newRequest = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer ${TokenStore.accessToken}")
                    .build()
                return chain.proceed(newRequest)
            } else {
                Log.w(TAG, "Token refresh failed, clearing tokens")
                TokenStore.clear()
            }
        }

        return response
    }

    private fun tryRefreshToken(chain: Interceptor.Chain): Boolean {
        return try {
            val refreshBody = Gson().toJson(RefreshTokenRequest(refreshToken = TokenStore.refreshToken))
            val mediaType = "application/json".toMediaType()
            val refreshRequest = okhttp3.Request.Builder()
                .url(chain.request().url.newBuilder()
                    .encodedPath("/api/v1/auth/reissue")
                    .build())
                .post(refreshBody.toRequestBody(mediaType))
                .addHeader("Content-Type", "application/json")
                .build()

            val refreshResponse = chain.proceed(refreshRequest)
            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string()
                refreshResponse.close()
                if (body != null) {
                    val type = object : TypeToken<ApiEnvelope<HostLoginResponse>>() {}.type
                    val envelope: ApiEnvelope<HostLoginResponse> = Gson().fromJson(body, type)
                    val data = envelope.data
                    if (data != null) {
                        TokenStore.accessToken = data.accessToken
                        TokenStore.refreshToken = data.refreshToken
                        Log.d(TAG, "Token refresh successful")
                        return true
                    }
                }
            } else {
                refreshResponse.close()
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh error", e)
            false
        }
    }
}
