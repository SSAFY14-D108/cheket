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

data class SignupUiState(
    val step: Int = 1,
    // Step 1
    val name: String = "",
    val email: String = "",
    val emailChecked: Boolean = false,
    val phone: String = "",
    val code: String = "",
    val codeSent: Boolean = false,
    val codeVerified: Boolean = false,
    // Step 2
    val password: String = "",
    val passwordConfirm: String = "",
    val agreedAll: Boolean = false,
    // State
    val errors: Map<String, String> = emptyMap(),
    val isSignupSuccess: Boolean = false,
)

class SignupViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init")
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errors = it.errors - "name") }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailChecked = false, errors = it.errors - "email") }
    }

    fun checkEmailDuplicate() {
        val state = _uiState.value
        Log.d(TAG, "checkEmailDuplicate() email=${state.email}")
        if (state.email.isBlank() || !state.email.contains("@")) {
            _uiState.update { it.copy(errors = it.errors + ("email" to "올바른 이메일을 입력해주세요.")) }
            return
        }
        // Mock: always available
        _uiState.update { it.copy(emailChecked = true, errors = it.errors - "email") }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errors = it.errors - "phone") }
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value, errors = it.errors - "code") }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errors = it.errors - "password") }
    }

    fun onPasswordConfirmChange(value: String) {
        _uiState.update { it.copy(passwordConfirm = value, errors = it.errors - "passwordConfirm") }
    }

    fun toggleAgreed() {
        _uiState.update { it.copy(agreedAll = !it.agreedAll, errors = it.errors - "agreed") }
    }

    fun sendSms() {
        val state = _uiState.value
        Log.d(TAG, "sendSms() phone=${state.phone}")
        if (state.phone.isBlank() || state.phone.length < 10) {
            Log.w(TAG, "sendSms() validation failed")
            _uiState.update { it.copy(errors = it.errors + ("phone" to "올바른 전화번호를 입력해주세요.")) }
            return
        }
        _uiState.update { it.copy(codeSent = true, errors = emptyMap()) }
    }

    fun verifyCode() {
        val state = _uiState.value
        Log.d(TAG, "verifyCode() codeLength=${state.code.length}")
        if (state.code.length == 6) {
            _uiState.update { it.copy(codeVerified = true, errors = emptyMap()) }
        } else {
            _uiState.update { it.copy(errors = it.errors + ("code" to "인증번호 6자리를 입력해주세요.")) }
        }
    }

    fun goToStep2() {
        val state = _uiState.value
        Log.d(TAG, "goToStep2() emailChecked=${state.emailChecked}, codeVerified=${state.codeVerified}")
        val newErrors = mutableMapOf<String, String>()
        if (state.name.isBlank()) newErrors["name"] = "이름을 입력해주세요."
        if (!state.emailChecked) newErrors["email"] = "이메일 중복확인이 필요합니다."
        if (!state.codeVerified) newErrors["code"] = "전화번호 인증이 필요합니다."
        if (newErrors.isNotEmpty()) {
            Log.w(TAG, "goToStep2() validation failed: ${newErrors.keys}")
            _uiState.update { it.copy(errors = newErrors) }
            return
        }
        _uiState.update { it.copy(step = 2, errors = emptyMap()) }
    }

    fun signup() {
        val state = _uiState.value
        Log.d(TAG, "signup() name=${state.name}")
        val newErrors = mutableMapOf<String, String>()
        if (state.password.length < 6) newErrors["password"] = "비밀번호는 6자 이상이어야 합니다."
        if (state.password != state.passwordConfirm) newErrors["passwordConfirm"] = "비밀번호가 일치하지 않습니다."
        if (!state.agreedAll) newErrors["agreed"] = "약관에 동의해주세요."
        if (newErrors.isNotEmpty()) {
            Log.w(TAG, "signup() validation failed: ${newErrors.keys}")
            _uiState.update { it.copy(errors = newErrors) }
            return
        }
        viewModelScope.launch {
            try {
                val success = authRepository.signup(state.name, state.phone, state.password)
                Log.d(TAG, "signup() result=$success")
                if (success) {
                    _uiState.update { it.copy(isSignupSuccess = true) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "signup() error", e)
            }
        }
    }

    companion object {
        private const val TAG = "SignupViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                SignupViewModel(app.appContainer.authRepository)
            }
        }
    }
}
