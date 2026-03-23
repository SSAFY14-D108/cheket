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
    @SerializedName("txId") val txId: Long? = null,
    @SerializedName("transactionId") val transactionId: Long? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("txStatus") val txStatus: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("txHash") val txHash: String? = null,
    @SerializedName("amount") val amount: Long? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("blockNumber") val blockNumber: Long? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
) {
    /** status 또는 txStatus 중 있는 값 반환 */
    val resolvedStatus: String get() = status ?: txStatus ?: ""
    val resolvedTxId: Long get() = txId ?: transactionId ?: 0L
}
