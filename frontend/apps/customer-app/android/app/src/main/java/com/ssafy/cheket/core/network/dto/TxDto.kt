package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

data class TxIdResponse(
    @SerializedName("txId") val txId: Long,
)
