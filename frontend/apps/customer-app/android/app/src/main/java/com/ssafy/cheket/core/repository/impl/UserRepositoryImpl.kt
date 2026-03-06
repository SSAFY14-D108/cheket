package com.ssafy.cheket.core.repository.impl

import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl : UserRepository {
    override fun getCurrentUser(): Flow<User> = flow { emit(MockDataSource.mockUser) }
}
