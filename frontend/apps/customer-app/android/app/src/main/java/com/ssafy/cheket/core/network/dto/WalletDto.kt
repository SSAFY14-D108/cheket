package com.ssafy.cheket.core.network.dto

import com.google.gson.annotations.SerializedName

data class BalanceResponse(
    @SerializedName("balance") val balance: Int,
    @SerializedName("walletAddress") val walletAddress: String,
)

data class TransactionDto(
    @SerializedName("transactionId") val transactionId: Long,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("description") val description: String?,
    @SerializedName("sellerId") val sellerId: Long? = null,
    @SerializedName("buyerId") val buyerId: Long? = null,
    @SerializedName("createdAt") val createdAt: String,
)

data class TxStatusDto(
    @SerializedName("txId") val txId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("type") val type: String,
    @SerializedName("txHash") val txHash: String?,
    @SerializedName("blockNumber") val blockNumber: Long?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
)
