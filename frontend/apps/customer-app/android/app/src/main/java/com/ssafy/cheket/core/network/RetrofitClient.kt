package com.ssafy.cheket.core.network

import com.ssafy.cheket.core.network.mock.MockInterceptor
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
    private lateinit var mockRetrofit: Retrofit

    fun init(authDataStore: AuthDataStore) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // ── Refresh client (토큰 재발급 전용) ──
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

        // ── Real client (서버 연결용) ──
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

        // ── Mock client (MockInterceptor로 HTTP 응답 mock) ──
        val mockClient = OkHttpClient.Builder()
            .addInterceptor(MockInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

        mockRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mockClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** 실제 서버 연결용 Retrofit */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    /** MockInterceptor가 붙은 Retrofit (FakeAppContainer에서 사용) */
    fun <T> createMockService(serviceClass: Class<T>): T {
        return mockRetrofit.create(serviceClass)
    }
}
