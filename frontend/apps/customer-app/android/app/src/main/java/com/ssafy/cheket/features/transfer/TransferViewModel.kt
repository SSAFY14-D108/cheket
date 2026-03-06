package com.ssafy.cheket.features.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransferUiState(
    val phone: String = "",
    val formattedPhone: String = "",
    val verifiedName: String? = null,
    val verifyError: String? = null,
    val isVerifying: Boolean = false,
    val isSubmitting: Boolean = false,
)

class TransferViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    fun onPhoneChange(raw: String) {
        // Only allow digits, max 11
        val digits = raw.filter { it.isDigit() }.take(11)
        val formatted = formatPhoneNumber(digits)
        _uiState.value = _uiState.value.copy(
            phone = digits,
            formattedPhone = formatted,
            verifiedName = null,
            verifyError = null,
        )
    }

    fun verifyRecipient() {
        val state = _uiState.value
        if (state.phone.length < 10) {
            _uiState.value = state.copy(verifyError = "올바른 전화번호를 입력해주세요")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, verifyError = null)
            delay(800) // simulate network

            val formatted = state.formattedPhone
            val name = MockDataSource.phoneBook[formatted]

            if (name != null) {
                _uiState.value = _uiState.value.copy(
                    verifiedName = name,
                    verifyError = null,
                    isVerifying = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    verifiedName = null,
                    verifyError = "해당 전화번호의 사용자를 찾을 수 없습니다",
                    isVerifying = false,
                )
            }
        }
    }

    fun submitTransfer(
        ticketId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val state = _uiState.value
        if (state.verifiedName == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            delay(1200) // simulate blockchain tx

            // Mock: 50% chance of success for demonstration; always succeed for
            // the first verified phone (010-9876-5432) and fail for 010-5555-4444
            val formatted = state.formattedPhone
            val willSucceed = formatted != "010-5555-4444"

            _uiState.value = _uiState.value.copy(isSubmitting = false)

            if (willSucceed) {
                onSuccess(ticketId)
            } else {
                onFailure(ticketId)
            }
        }
    }

    private fun formatPhoneNumber(digits: String): String {
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
            else -> "${digits.substring(0, 3)}-${digits.substring(3, 7)}-${digits.substring(7)}"
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TransferViewModel() }
        }
    }
}
