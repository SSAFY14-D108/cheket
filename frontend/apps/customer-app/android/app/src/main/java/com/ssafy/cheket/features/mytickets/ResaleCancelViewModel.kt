package com.ssafy.cheket.features.mytickets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.safeCall
import com.ssafy.cheket.core.network.service.ResaleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResaleCancelUiState(
    val isCancelling: Boolean = false,
)

class ResaleCancelViewModel(
    private val resaleService: ResaleService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResaleCancelUiState())
    val uiState: StateFlow<ResaleCancelUiState> = _uiState.asStateFlow()

    fun cancelResale(
        ticketId: String,
        onSuccess: (Long) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            onFailure("잘못된 티켓 정보입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ResaleCancelUiState(isCancelling = true)

            val result = safeCall { resaleService.cancelResale(ticketIdLong) }

            _uiState.value = ResaleCancelUiState(isCancelling = false)

            result
                .onSuccess { response ->
                    Log.d(TAG, "cancelResale() statusCode=${response.httpStatusCode}, txId=${response.data?.txId}")
                    val txId = response.data?.txId
                    if (response.httpStatusCode in 200..299 && txId != null) {
                        onSuccess(txId)
                    } else {
                        onFailure(response.responseMessage ?: "판매 등록 취소 요청에 실패했습니다.")
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "판매 등록 취소 중 오류가 발생했습니다."
                    Log.e(TAG, "cancelResale() failed: $message", throwable)
                    onFailure(message)
                }
        }
    }

    companion object {
        private const val TAG = "ResaleCancelViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ResaleCancelViewModel(app.appContainer.resaleService)
            }
        }
    }
}
