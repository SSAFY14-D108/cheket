package com.example.cheketqr.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStore.accessToken
        val requestBuilder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")

        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
