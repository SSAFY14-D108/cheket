package com.ssafy.cheket.core.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthAuthenticator(
    private val authDataStore: AuthDataStore,
    private val refreshService: RefreshService
) : Authenticator {

    private val maxRetries = 2

    override fun authenticate(route: Route?, response: Response): Request? {
        // 로그아웃 중이면 토큰 갱신 차단
        if (authDataStore.isLoggingOut) {
            Log.d(TAG, "authenticate() — logging out, skip refresh")
            return null
        }

        val retryCount = responseCount(response)
        Log.d(TAG, "authenticate() — retryCount=$retryCount, url=${response.request.url}")

        if (retryCount >= maxRetries) {
            Log.w(TAG, "authenticate() — max retries reached, forcing logout")
            authDataStore.clear()
            AuthEventBus.sendEvent(
                AuthEvent.ForceLogout(AuthLogoutReason.REFRESH_FAILED)
            )
            return null
        }

        val refreshToken = authDataStore.getRefreshToken() ?: run {
            Log.w(TAG, "authenticate() — no refresh token, forcing logout")
            authDataStore.clear()
            AuthEventBus.sendEvent(
                AuthEvent.ForceLogout(AuthLogoutReason.TOKEN_EXPIRED)
            )
            return null
        }

        Log.d(TAG, "authenticate() — attempting token refresh, refreshToken=${refreshToken.take(20)}...")
        val newTokens = runBlocking {
            try {
                val reissueResponse = refreshService.reissue(ReissueRequest(refreshToken))
                Log.d(TAG, "authenticate() — reissue statusCode=${reissueResponse.httpStatusCode}, data=${reissueResponse.data != null}")
                reissueResponse.data?.let { response ->
                    AuthTokens(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "authenticate() — refresh failed: ${e.javaClass.simpleName}: ${e.message}", e)
                null
            }
        }

        return if (newTokens != null) {
            Log.d(TAG, "authenticate() — token refresh success")
            authDataStore.saveTokens(newTokens)
            response.request.newBuilder()
                .header("Authorization", "${newTokens.tokenType} ${newTokens.accessToken}")
                .build()
        } else {
            Log.w(TAG, "authenticate() — token refresh returned null, forcing logout")
            authDataStore.clear()
            AuthEventBus.sendEvent(
                AuthEvent.ForceLogout(AuthLogoutReason.REFRESH_FAILED)
            )
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private const val TAG = "AuthAuthenticator"
    }
}
