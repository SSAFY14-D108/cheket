package com.ssafy.cheket.features.resale

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.network.dto.ResaleRegisterRequest
import com.ssafy.cheket.core.network.safeCall
import com.ssafy.cheket.core.network.service.ResaleService
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResaleCreateUiState(
    val ticket: Ticket? = null,
    val isLoadingTicket: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitMessage: String? = null,
    val transactionId: Long? = null,
    val errorMessage: String? = null,
)

class ResaleCreateViewModel(
    private val ticketId: String,
    private val ticketRepository: TicketRepository,
    private val resaleService: ResaleService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResaleCreateUiState())
    val uiState: StateFlow<ResaleCreateUiState> = _uiState.asStateFlow()

    init {
        loadTicket()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun submitResale(resalePrice: Int) {
        val ticket = _uiState.value.ticket
        if (ticket == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "티켓 정보를 먼저 불러와주세요.")
            return
        }

        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "잘못된 티켓 정보입니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val result = safeCall {
                resaleService.registerResale(
                    ticketId = ticketIdLong,
                    request = ResaleRegisterRequest(resalePrice = resalePrice),
                )
            }

            result
                .onSuccess { response ->
                    Log.d(TAG, "submitResale() statusCode=${response.httpStatusCode}, txId=${response.data?.txId}")
                    _uiState.value = if (response.httpStatusCode in 200..299) {
                        _uiState.value.copy(
                            isSubmitting = false,
                            submitMessage = response.responseMessage ?: "2차 판매 등록 요청이 접수되었습니다.",
                            transactionId = response.data?.txId,
                            errorMessage = null,
                        )
                    } else {
                        _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = response.responseMessage ?: "2차 판매 등록에 실패했습니다.",
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "2차 판매 등록 중 오류가 발생했습니다."
                    Log.e(TAG, "submitResale() failed: $message", throwable)
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = message,
                    )
                }
        }
    }

    private fun loadTicket() {
        val cachedTicket = NavParams.selectedTicket?.takeIf { it.id == ticketId }
        if (cachedTicket != null) {
            _uiState.value = _uiState.value.copy(ticket = cachedTicket, isLoadingTicket = false)
            return
        }

        viewModelScope.launch {
            val ticket = runCatching { ticketRepository.getTicketById(ticketId) }
                .onFailure { Log.e(TAG, "loadTicket() failed", it) }
                .getOrNull()

            _uiState.value = if (ticket != null) {
                _uiState.value.copy(ticket = ticket, isLoadingTicket = false)
            } else {
                _uiState.value.copy(
                    isLoadingTicket = false,
                    errorMessage = "티켓 정보를 불러오지 못했습니다.",
                )
            }
        }
    }

    companion object {
        private const val TAG = "ResaleCreateViewModel"

        fun factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ResaleCreateViewModel(
                    ticketId = ticketId,
                    ticketRepository = app.appContainer.ticketRepository,
                    resaleService = app.appContainer.resaleService,
                )
            }
        }
    }
}
