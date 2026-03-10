package com.ssafy.cheket.core.network

import com.google.gson.annotations.SerializedName
import com.ssafy.cheket.core.network.dto.ApiResponse
import com.ssafy.cheket.core.network.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class ReissueRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

interface RefreshService {
    @POST("api/v1/auth/reissue")
    suspend fun reissue(
        @Body request: ReissueRequest
    ): ApiResponse<LoginResponse>
}
