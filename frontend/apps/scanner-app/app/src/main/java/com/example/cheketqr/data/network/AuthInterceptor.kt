package com.example.cheketqr.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val accessToken: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")

        if (accessToken.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
