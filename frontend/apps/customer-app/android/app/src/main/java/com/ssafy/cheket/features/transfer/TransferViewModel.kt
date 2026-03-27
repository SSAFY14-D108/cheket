package com.ssafy.cheket.features.transfer

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.safeCall
import com.ssafy.cheket.core.network.dto.TransferRequest
import com.ssafy.cheket.core.network.service.AuthService
import com.ssafy.cheket.core.network.service.TicketService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransferUiState(
    val phone: String = "",
    val formattedPhone: String = "",
    val phoneError: String? = null,
    val isSubmitting: Boolean = false,
    val isSearching: Boolean = false,
    /** 수신자 확인 다이얼로그 — non-null이면 표시 */
    val recipientConfirm: RecipientConfirm? = null,
)

data class RecipientConfirm(
    val name: String,
    val phone: String,
)

class TransferViewModel(
    private val ticketService: TicketService,
    private val authService: AuthService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    /** 양도 시 사용할 콜백 (확인 다이얼로그 OK 후 실행) */
    private var pendingTicketId: String? = null
    private var pendingOnSuccess: ((String, Long?) -> Unit)? = null
    private var pendingOnFailure: ((String, String) -> Unit)? = null

    fun onPhoneChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(11)
        _uiState.value = _uiState.value.copy(
            phone = digits,
            formattedPhone = formatPhoneNumber(digits),
            phoneError = null,
        )
    }

    /**
     * 양도 버튼 → 먼저 수신자 조회, 확인 다이얼로그 표시
     */
    fun requestTransfer(
        ticketId: String,
        onSuccess: (String, Long?) -> Unit,
        onFailure: (String, String) -> Unit,
    ) {
        val state = _uiState.value
        if (state.phone.length !in 10..11) {
            _uiState.value = state.copy(phoneError = "전화번호를 정확히 입력해주세요.")
            return
        }

        pendingTicketId = ticketId
        pendingOnSuccess = onSuccess
        pendingOnFailure = onFailure

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, phoneError = null)

            val result = safeCall {
                authService.searchUser(
                    userType = "USER",
                    number = state.formattedPhone,
                )
            }

            result
                .onSuccess { response ->
                    Log.d(TAG, "searchUser() statusCode=${response.httpStatusCode}")
                    if (response.httpStatusCode in 200..299 && response.data != null) {
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            recipientConfirm = RecipientConfirm(
                                name = response.data.name,
                                phone = response.data.number,
                            ),
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            phoneError = response.responseMessage ?: "해당 전화번호의 사용자를 찾을 수 없습니다.",
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "searchUser() failed", throwable)
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        phoneError = "사용자 조회에 실패했습니다.",
                    )
                }
        }
    }

    /** 확인 다이얼로그에서 "양도하기" 클릭 */
    fun confirmTransfer() {
        val ticketId = pendingTicketId ?: return
        val onSuccess = pendingOnSuccess ?: return
        val onFailure = pendingOnFailure ?: return

        dismissConfirmDialog()
        submitTransfer(ticketId, onSuccess, onFailure)
    }

    /** 확인 다이얼로그 닫기 */
    fun dismissConfirmDialog() {
        _uiState.value = _uiState.value.copy(recipientConfirm = null)
    }

    private fun submitTransfer(
        ticketId: String,
        onSuccess: (String, Long?) -> Unit,
        onFailure: (String, String) -> Unit,
    ) {
        val state = _uiState.value

        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            onFailure(ticketId, "잘못된 티켓 정보입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, phoneError = null)

            val result = safeCall {
                ticketService.transferTicket(
                    ticketId = ticketIdLong,
                    request = TransferRequest(phoneNumber = state.formattedPhone),
                )
            }

            _uiState.value = _uiState.value.copy(isSubmitting = false)

            result
                .onSuccess { response ->
                    Log.d(TAG, "submitTransfer() statusCode=${response.httpStatusCode}")
                    if (response.httpStatusCode in 200..299) {
                        onSuccess(ticketId, response.data?.txId)
                    } else {
                        onFailure(ticketId, response.responseMessage ?: "양도에 실패했습니다.")
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "양도 처리 중 오류가 발생했습니다."
                    Log.e(TAG, "submitTransfer() failed: $message", throwable)
                    onFailure(ticketId, message)
                }
        }
    }

    private fun formatPhoneNumber(digits: String): String = when {
        digits.length <= 3 -> digits
        digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
    }

    companion object {
        private const val TAG = "TransferViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                TransferViewModel(app.appContainer.ticketService, app.appContainer.authService)
            }
        }
    }
}
