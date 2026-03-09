package com.ssafy.cheket.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val TIMEOUT_SECONDS = 5L

    private lateinit var regularClient: OkHttpClient
    private lateinit var refreshClient: OkHttpClient
    private lateinit var retrofit: Retrofit
    private lateinit var refreshRetrofit: Retrofit

    fun init(authDataStore: AuthDataStore) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        refreshClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        refreshRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val refreshService = refreshRetrofit.create(RefreshService::class.java)

        regularClient = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(authDataStore))
            .authenticator(AuthAuthenticator(authDataStore, refreshService))
            .addInterceptor(loggingInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(regularClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
