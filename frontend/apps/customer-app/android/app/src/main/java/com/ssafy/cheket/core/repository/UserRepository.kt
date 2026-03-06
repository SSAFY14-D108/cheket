package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User>
}
