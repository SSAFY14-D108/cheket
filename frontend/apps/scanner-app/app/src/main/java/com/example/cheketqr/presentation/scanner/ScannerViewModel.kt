package com.example.cheketqr.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cheketqr.data.model.VerifyTicketData
import com.example.cheketqr.domain.repository.VerifyTicketResult
import com.example.cheketqr.domain.usecase.VerifyQrUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val hasCameraPermission: Boolean = false,
    val isVerifying: Boolean = false,
    val isScannerEnabled: Boolean = true,
    val resultDialog: ScanResultDialogState? = null
)

sealed interface ScanResultDialogState {
    data class Success(
        val message: String,
        val data: VerifyTicketData
    ) : ScanResultDialogState

    data class Failure(
        val message: String
    ) : ScanResultDialogState
}

class ScannerViewModel(
    private val verifyQrUseCase: VerifyQrUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onCameraPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun onQrDetected(rawQrValue: String) {
        val state = _uiState.value
        if (!state.hasCameraPermission || state.isVerifying || !state.isScannerEnabled) {
            return
        }

        _uiState.update {
            it.copy(
                isScannerEnabled = false,
                isVerifying = true
            )
        }

        val ticketId = parseTicketId(rawQrValue)
        if (ticketId == null) {
            showFailure("Invalid QR format.")
            return
        }

        viewModelScope.launch {
            when (val result = verifyQrUseCase(ticketId, rawQrValue)) {
                is VerifyTicketResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            resultDialog = ScanResultDialogState.Success(
                                message = result.message,
                                data = result.data
                            )
                        )
                    }
                }

                is VerifyTicketResult.Failure -> {
                    showFailure(result.message)
                }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update {
            it.copy(
                resultDialog = null,
                isScannerEnabled = true,
                isVerifying = false
            )
        }
    }

    private fun showFailure(message: String) {
        _uiState.update {
            it.copy(
                isVerifying = false,
                resultDialog = ScanResultDialogState.Failure(message)
            )
        }
    }

    private fun parseTicketId(rawQrValue: String): Long? {
        val ticket = rawQrValue.substringBefore(':', missingDelimiterValue = "")
        return ticket.toLongOrNull()
    }
}
