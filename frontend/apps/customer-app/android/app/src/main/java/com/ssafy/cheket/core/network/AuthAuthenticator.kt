package com.ssafy.cheket.core.network

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
        if (responseCount(response) >= maxRetries) {
            authDataStore.clear()
            AuthEventBus.sendEvent(
                AuthEvent.ForceLogout(AuthLogoutReason.REFRESH_FAILED)
            )
            return null
        }

        val refreshToken = authDataStore.getRefreshToken() ?: run {
            authDataStore.clear()
            AuthEventBus.sendEvent(
                AuthEvent.ForceLogout(AuthLogoutReason.TOKEN_EXPIRED)
            )
            return null
        }

        val newTokens = runBlocking {
            try {
                refreshService.reissue(ReissueRequest(refreshToken)).data?.let { response ->
                    AuthTokens(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                    )
                }
            } catch (e: Exception) {
                null
            }
        }

        return if (newTokens != null) {
            authDataStore.saveTokens(newTokens)
            response.request.newBuilder()
                .header("Authorization", "${newTokens.tokenType} ${newTokens.accessToken}")
                .build()
        } else {
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
}
