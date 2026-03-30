package com.ssafy.cheket.features.mytickets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.safeCall
import com.ssafy.cheket.core.network.service.TicketService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketRefundUiState(
    val isRefunding: Boolean = false,
)

class TicketRefundViewModel(
    private val ticketService: TicketService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TicketRefundUiState())
    val uiState: StateFlow<TicketRefundUiState> = _uiState.asStateFlow()

    fun refundTicket(
        ticketId: String,
        onSuccess: (txId: Long?) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            onFailure("잘못된 티켓 정보입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = TicketRefundUiState(isRefunding = true)

            val result = safeCall { ticketService.refundTicket(ticketIdLong) }

            _uiState.value = TicketRefundUiState(isRefunding = false)

            result
                .onSuccess { response ->
                    Log.d(TAG, "refundTicket() statusCode=${response.httpStatusCode}")
                    if (response.httpStatusCode == 200) {
                        val txId = response.data?.txId
                        Log.d(TAG, "refundTicket() success, txId=$txId")
                        onSuccess(txId)
                    } else {
                        onFailure(response.responseMessage ?: "티켓 환불에 실패했습니다.")
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "환불 처리 중 오류가 발생했습니다."
                    Log.e(TAG, "refundTicket() failed: $message", throwable)
                    onFailure(message)
                }
        }
    }

    companion object {
        private const val TAG = "TicketRefundViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                TicketRefundViewModel(app.appContainer.ticketService)
            }
        }
    }
}
