package com.ssafy.cheket.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authDataStore: AuthDataStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        if (isAuthEndpoint(url)) {
            return chain.proceed(originalRequest)
        }

        val token = authDataStore.getAccessToken()
        val tokenType = authDataStore.getTokenType()

        if (token == null) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "$tokenType $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun isAuthEndpoint(url: String): Boolean {
        return url.contains("/auth/login") ||
                url.contains("/auth/reissue") ||
                url.contains("/auth/signup")
    }
}
