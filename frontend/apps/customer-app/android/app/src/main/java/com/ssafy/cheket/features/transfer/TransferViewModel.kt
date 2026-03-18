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
)

class TransferViewModel(
    private val ticketService: TicketService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    fun onPhoneChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(11)
        _uiState.value = _uiState.value.copy(
            phone = digits,
            formattedPhone = formatPhoneNumber(digits),
            phoneError = null,
        )
    }

    fun submitTransfer(
        ticketId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String, String) -> Unit,
    ) {
        val state = _uiState.value
        if (state.phone.length !in 10..11) {
            _uiState.value = state.copy(phoneError = "전화번호를 정확히 입력해주세요.")
            return
        }

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
                    if (response.httpStatusCode == 200) {
                        onSuccess(ticketId)
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
                TransferViewModel(app.appContainer.ticketService)
            }
        }
    }
}
