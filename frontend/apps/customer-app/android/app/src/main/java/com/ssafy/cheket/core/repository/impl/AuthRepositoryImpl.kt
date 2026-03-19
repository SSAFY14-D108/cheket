package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.network.AuthDataStore
import com.ssafy.cheket.core.network.AuthEventBus
import com.ssafy.cheket.core.network.AuthEvent
import com.ssafy.cheket.core.network.AuthLogoutReason
import com.ssafy.cheket.core.network.AuthTokens
import com.ssafy.cheket.core.network.dto.AuthChangePasswordRequest
import com.ssafy.cheket.core.network.dto.LoginRequest
import com.ssafy.cheket.core.network.dto.SignupRequest
import com.ssafy.cheket.core.network.service.AuthService
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthRepositoryImpl"

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userService: UserService,
    private val authDataStore: AuthDataStore,
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authDataStore.isLoggedIn())
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // 저장된 토큰이 있으면 유저 정보 복원
        if (authDataStore.isLoggedIn()) {
            Log.d(TAG, "init — saved tokens found, restoring session")
            CoroutineScope(Dispatchers.IO).launch {
                fetchAndSetCurrentUser()
            }
        }
    }

    override suspend fun login(id: String, password: String): Boolean {
        Log.d(TAG, "login() id=$id")
        return try {
            val response = authService.login(LoginRequest(email = id, password = password))
            Log.d(TAG, "login() statusCode=${response.httpStatusCode}")
            if (response.httpStatusCode in 200..299 && response.data != null) {
                val tokens = AuthTokens(
                    accessToken = response.data.accessToken,
                    refreshToken = response.data.refreshToken,
                )
                authDataStore.saveTokens(tokens)
                Log.d(TAG, "login() success, tokens saved. Fetching user info...")
                fetchAndSetCurrentUser()
                _isLoggedIn.value = true
                true
            } else {
                Log.w(TAG, "login() failed: ${response.responseMessage}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "login() error", e)
            false
        }
    }

    override suspend fun signup(name: String, phone: String, password: String): Boolean {
        Log.d(TAG, "signup() name=$name")
        return try {
            // NOTE: 현재 인터페이스에 email 파라미터가 없으므로 빈 문자열로 전달
            // TODO: AuthRepository 인터페이스에 email 파라미터 추가 필요
            val response = authService.signup(
                SignupRequest(
                    username = name,
                    phoneNumber = phone,
                    email = "",
                    password = password,
                )
            )
            Log.d(TAG, "signup() statusCode=${response.httpStatusCode}")
            response.httpStatusCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "signup() error", e)
            false
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> {
        Log.d(TAG, "changePassword()")
        return try {
            val response = authService.changePassword(
                AuthChangePasswordRequest(oldPassword = oldPassword, newPassword = newPassword)
            )
            Log.d(TAG, "changePassword() statusCode=${response.httpStatusCode}")
            if (response.httpStatusCode in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.responseMessage ?: "비밀번호 변경 실패"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "changePassword() error", e)
            Result.failure(e)
        }
    }

    override fun logout() {
        Log.d(TAG, "logout()")
        authDataStore.clear()
        _currentUser.value = null
        _isLoggedIn.value = false
        AuthEventBus.sendEvent(AuthEvent.ForceLogout(AuthLogoutReason.USER_LOGOUT))
    }

    private suspend fun fetchAndSetCurrentUser() {
        try {
            val response = userService.getUserInfo()
            Log.d(TAG, "fetchAndSetCurrentUser() statusCode=${response.httpStatusCode}")
            response.data?.let { dto ->
                _currentUser.value = User(
                    id = dto.userId.toString(),
                    name = dto.username,
                    phone = dto.phoneNumber,
                    email = dto.email,
                    walletAddress = "", // TODO: 지갑 주소는 별도 API로 조회
                    ctkBalance = 0,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAndSetCurrentUser() error", e)
        }
    }
}
