package com.ssafy.cheket.core.repository.fake

import android.util.Log
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "FakeUserRepo"

class FakeUserRepository : UserRepository {
    override fun getCurrentUser(): Flow<User> = flow {
        Log.d(TAG, "getCurrentUser()")
        emit(MockDataSource.mockUser)
    }
}
