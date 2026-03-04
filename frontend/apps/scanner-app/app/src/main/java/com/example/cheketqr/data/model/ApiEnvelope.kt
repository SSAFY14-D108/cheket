package com.example.cheketqr.data.model

data class ApiEnvelope<T>(
    val httpStatusCode: Int? = null,
    val responseMessage: String? = null,
    val errorMessage: String? = null,
    val data: T? = null
)
