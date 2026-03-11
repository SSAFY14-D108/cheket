package com.ssafy.cheket.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val id: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val error: String = "",
    val isLoginSuccess: Boolean = false,
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init")
    }

    fun onIdChange(value: String) {
        _uiState.update { it.copy(id = value, error = "") }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = "") }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        val state = _uiState.value
        Log.d(TAG, "login() id=${state.id}")
        if (state.id.isBlank() || state.password.isBlank()) {
            Log.w(TAG, "login() validation failed — empty fields")
            _uiState.update { it.copy(error = "아이디와 비밀번호를 입력해주세요.") }
            return
        }
        viewModelScope.launch {
            try {
                val success = authRepository.login(state.id, state.password)
                Log.d(TAG, "login() result=$success")
                if (success) {
                    _uiState.update { it.copy(isLoginSuccess = true, error = "") }
                } else {
                    _uiState.update { it.copy(error = "아이디 또는 비밀번호가 잘못되었습니다.") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "login() error", e)
                _uiState.update { it.copy(error = "로그인 중 오류가 발생했습니다.") }
            }
        }
    }

    companion object {
        private const val TAG = "LoginViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                LoginViewModel(app.appContainer.authRepository)
            }
        }
    }
}
