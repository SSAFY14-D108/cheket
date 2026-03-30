package com.ssafy.cheket.core.repository.fake

import android.util.Log
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "FakeAuthRepo"

class FakeAuthRepository : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun login(id: String, password: String): Boolean {
        Log.d(TAG, "login() id=$id")
        if (id.isBlank()) return false
        _currentUser.value = MockDataSource.mockUser
        _isLoggedIn.value = true
        return true
    }

    override suspend fun signup(name: String, phone: String, password: String): Boolean {
        Log.d(TAG, "signup() name=$name")
        if (password.length < 6) return false
        val newUser = User(
            id = "new_user_1",
            name = name,
            phone = phone,
            email = "$name@cheket.app",
            walletAddress = "0xNewUser",
            ctkBalance = 0,
        )
        _currentUser.value = newUser
        _isLoggedIn.value = true
        return true
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        Log.d(TAG, "changePassword()")
        return Result.success(Unit)
    }

    override fun logout() {
        Log.d(TAG, "logout()")
        _currentUser.value = null
        _isLoggedIn.value = false
    }
}
