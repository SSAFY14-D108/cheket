package com.ssafy.cheket.features.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.EmailDuplicateRequest
import com.ssafy.cheket.core.network.dto.SmsSendRequest
import com.ssafy.cheket.core.network.dto.SmsVerifyRequest
import com.ssafy.cheket.core.network.dto.LoginRequest
import com.ssafy.cheket.core.network.dto.SignupRequest
import com.ssafy.cheket.core.network.AuthTokens
import com.ssafy.cheket.core.network.service.AuthService
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
    val showPassword: Boolean = false,
    val showPasswordConfirm: Boolean = false,
    val agreedAll: Boolean = false,
    // State
    val errors: Map<String, String> = emptyMap(),
    val isSignupSuccess: Boolean = false,
    val isLoading: Boolean = false,
)

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val authService: AuthService,
    private val authDataStore: com.ssafy.cheket.core.network.AuthDataStore,
) : ViewModel() {

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
        viewModelScope.launch {
            try {
                val response = authService.checkEmailDuplicate(EmailDuplicateRequest(state.email))
                Log.d(TAG, "checkEmailDuplicate() statusCode=${response.httpStatusCode}")
                if (response.httpStatusCode in 200..299) {
                    _uiState.update { it.copy(emailChecked = true, errors = it.errors - "email") }
                } else {
                    _uiState.update { it.copy(errors = it.errors + ("email" to (response.responseMessage ?: "이미 사용 중인 이메일입니다."))) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkEmailDuplicate() error", e)
                _uiState.update { it.copy(errors = it.errors + ("email" to "이메일 확인에 실패했습니다.")) }
            }
        }
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

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun togglePasswordConfirmVisibility() {
        _uiState.update { it.copy(showPasswordConfirm = !it.showPasswordConfirm) }
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
        viewModelScope.launch {
            try {
                val response = authService.sendSms(SmsSendRequest(com.ssafy.cheket.core.ui.component.formatPhoneForApi(state.phone)))
                Log.d(TAG, "sendSms() statusCode=${response.httpStatusCode}")
                if (response.httpStatusCode in 200..299) {
                    _uiState.update { it.copy(codeSent = true, errors = emptyMap()) }
                } else {
                    _uiState.update { it.copy(errors = it.errors + ("phone" to (response.responseMessage ?: "SMS 발송 실패"))) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendSms() error", e)
                _uiState.update { it.copy(errors = it.errors + ("phone" to "SMS 발송에 실패했습니다.")) }
            }
        }
    }

    fun verifyCode() {
        val state = _uiState.value
        Log.d(TAG, "verifyCode() codeLength=${state.code.length}")
        if (state.code.length != 6) {
            _uiState.update { it.copy(errors = it.errors + ("code" to "인증번호 6자리를 입력해주세요.")) }
            return
        }
        viewModelScope.launch {
            try {
                val response = authService.verifySms(SmsVerifyRequest(com.ssafy.cheket.core.ui.component.formatPhoneForApi(state.phone), state.code))
                Log.d(TAG, "verifyCode() statusCode=${response.httpStatusCode}")
                if (response.httpStatusCode in 200..299 && response.data?.verified == true) {
                    _uiState.update { it.copy(codeVerified = true, errors = emptyMap()) }
                } else {
                    _uiState.update { it.copy(errors = it.errors + ("code" to "인증번호가 올바르지 않습니다.")) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "verifyCode() error", e)
                _uiState.update { it.copy(errors = it.errors + ("code" to "인증 확인에 실패했습니다.")) }
            }
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
        // 약관 동의 UI 제거됨 — agreedAll 검사 생략
        if (newErrors.isNotEmpty()) {
            Log.w(TAG, "signup() validation failed: ${newErrors.keys}")
            _uiState.update { it.copy(errors = newErrors) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = authService.signup(
                    SignupRequest(
                        username = state.name,
                        phoneNumber = com.ssafy.cheket.core.ui.component.formatPhoneForApi(state.phone),
                        email = state.email,
                        password = state.password,
                    )
                )
                Log.d(TAG, "signup() statusCode=${response.httpStatusCode}")
                if (response.httpStatusCode in 200..299) {
                    // 회원가입 성공 후 자동 로그인
                    try {
                        val loginResponse = authService.login(LoginRequest(email = state.email, password = state.password))
                        if (loginResponse.httpStatusCode in 200..299 && loginResponse.data != null) {
                            authDataStore.onLoginSuccess()
                            authDataStore.saveTokens(
                                AuthTokens(
                                    accessToken = loginResponse.data.accessToken,
                                    refreshToken = loginResponse.data.refreshToken,
                                )
                            )
                            Log.d(TAG, "signup() auto-login success")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "signup() auto-login failed (non-critical)", e)
                    }
                    _uiState.update { it.copy(isSignupSuccess = true, isLoading = false) }
                } else {
                    _uiState.update { it.copy(errors = mapOf("signup" to (response.responseMessage ?: "회원가입 실패")), isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "signup() error", e)
                _uiState.update { it.copy(errors = mapOf("signup" to "회원가입에 실패했습니다."), isLoading = false) }
            }
        }
    }

    companion object {
        private const val TAG = "SignupViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                SignupViewModel(app.appContainer.authRepository, app.appContainer.authService, app.authDataStore)
            }
        }
    }
}
