package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "UserRepositoryImpl"

class UserRepositoryImpl(
    private val userService: UserService,
) : UserRepository {

    override fun getCurrentUser(): Flow<User> = flow {
        Log.d(TAG, "getCurrentUser()")
        try {
            val response = userService.getUserInfo()
            Log.d(TAG, "getCurrentUser() statusCode=${response.httpStatusCode}")
            response.data?.let { dto ->
                emit(
                    User(
                        id = dto.userId.toString(),
                        name = dto.username,
                        phone = dto.phoneNumber,
                        email = dto.email,
                        walletAddress = "", // TODO: 지갑 주소는 WalletService에서 조회
                        ctkBalance = 0,
                    )
                )
            } ?: throw IllegalStateException("User info not found")
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentUser() error", e)
            throw e
        }
    }
}
