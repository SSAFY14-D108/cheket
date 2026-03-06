package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    val isLoggedIn: StateFlow<Boolean>

    suspend fun login(id: String, password: String): Boolean
    suspend fun signup(name: String, phone: String, password: String): Boolean
    fun logout()
}
