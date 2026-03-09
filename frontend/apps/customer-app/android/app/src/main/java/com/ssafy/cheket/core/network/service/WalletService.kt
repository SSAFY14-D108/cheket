package com.ssafy.cheket.core.network.service

import com.ssafy.cheket.core.network.dto.*
import retrofit2.http.*

interface WalletService {

    @GET("api/v1/wallets/balance")
    suspend fun getBalance(): ApiResponse<BalanceResponse>

    @GET("api/v1/wallets/transactions")
    suspend fun getTransactions(): ApiResponse<List<TransactionDto>>

    @GET("api/v1/wallets/transactions/{txId}")
    suspend fun getTransactionStatus(@Path("txId") txId: Long): ApiResponse<TxStatusDto>
}
