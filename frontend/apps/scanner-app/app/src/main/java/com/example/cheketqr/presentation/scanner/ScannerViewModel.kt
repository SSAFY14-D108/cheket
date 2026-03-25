package com.example.cheketqr.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cheketqr.data.model.CheckInResponse
import com.example.cheketqr.domain.repository.CheckInResult
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
    val resultDialog: ScanResultDialogState? = null,
)

sealed interface ScanResultDialogState {
    data class Success(
        val message: String,
        val data: CheckInResponse,
    ) : ScanResultDialogState

    data class Failure(
        val message: String,
    ) : ScanResultDialogState
}

class ScannerViewModel(
    private val verifyQrUseCase: VerifyQrUseCase,
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

        // QR 토큰은 JWT 문자열 — 그대로 check-in API에 전달
        if (rawQrValue.isBlank()) {
            showFailure("빈 QR 코드입니다.")
            return
        }

        _uiState.update {
            it.copy(isScannerEnabled = false, isVerifying = true)
        }

        viewModelScope.launch {
            when (val result = verifyQrUseCase(rawQrValue)) {
                is CheckInResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            resultDialog = ScanResultDialogState.Success(
                                message = result.message,
                                data = result.data,
                            ),
                        )
                    }
                }
                is CheckInResult.Failure -> {
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
                isVerifying = false,
            )
        }
    }

    private fun showFailure(message: String) {
        _uiState.update {
            it.copy(
                isVerifying = false,
                resultDialog = ScanResultDialogState.Failure(message),
            )
        }
    }
}
