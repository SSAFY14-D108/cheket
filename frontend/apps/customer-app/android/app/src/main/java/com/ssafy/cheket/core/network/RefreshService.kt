package com.ssafy.cheket.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class ReissueRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

interface RefreshService {
    @POST("auth/reissue")
    suspend fun reissue(
        @Body request: ReissueRequest
    ): AuthTokens
}
