package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("httpStatusCode") val httpStatusCode: Int,
    @SerializedName("responseMessage") val responseMessage: String? = null,
    @SerializedName("data") val data: T? = null,
)
