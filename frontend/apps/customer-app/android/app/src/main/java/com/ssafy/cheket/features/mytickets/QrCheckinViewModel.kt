package com.ssafy.cheket.features.mytickets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.network.safeCall
import com.ssafy.cheket.core.network.service.TicketService
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrCheckinUiState(
    val ticket: Ticket? = null,
    val title: String = "",
    val seatLabel: String = "",
    val qrData: String? = null,
    val expiresAt: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class QrCheckinViewModel(
    private val ticketId: String,
    private val ticketRepository: TicketRepository,
    private val ticketService: TicketService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QrCheckinUiState())
    val uiState: StateFlow<QrCheckinUiState> = _uiState.asStateFlow()

    init {
        loadTicket()
        refreshQr()
    }

    fun refreshQr() {
        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = "잘못된 티켓 정보입니다.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.qrData == null,
                isRefreshing = true,
                errorMessage = null,
            )

            val result = safeCall { ticketService.generateQr(ticketIdLong) }

            result
                .onSuccess { response ->
                    val data = response.data
                    Log.d(TAG, "refreshQr() statusCode=${response.httpStatusCode}, hasData=${data != null}")

                    if (response.httpStatusCode in 200..299 && data != null) {
                        val currentTicket = _uiState.value.ticket
                        _uiState.value = _uiState.value.copy(
                            title = data.title.ifBlank { currentTicket?.showName.orEmpty() },
                            seatLabel = listOf(data.sectionName, data.seatNo).filter { it.isNotBlank() }.joinToString(" "),
                            qrData = data.qrData,
                            expiresAt = data.expiresAt,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = response.responseMessage ?: "QR 코드를 불러오지 못했습니다.",
                        )
                    }
                }
                .onFailure { throwable ->
                    val rawMessage = throwable.message ?: "QR 코드를 불러오지 못했습니다."
                    val message = if (rawMessage.contains("HTTP 404")) {
                        "백엔드에 QR 코드 API가 아직 연결되지 않았습니다."
                    } else {
                        rawMessage
                    }
                    Log.e(TAG, "refreshQr() failed: $message", throwable)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = message,
                    )
                }
        }
    }

    private fun loadTicket() {
        val cachedTicket = NavParams.selectedTicket?.takeIf { it.id == ticketId }
        if (cachedTicket != null) {
            _uiState.value = _uiState.value.copy(
                ticket = cachedTicket,
                title = cachedTicket.showName,
                seatLabel = cachedTicket.seatLabel,
                isLoading = true,
            )
            return
        }

        viewModelScope.launch {
            val ticket = runCatching { ticketRepository.getTicketById(ticketId) }
                .onFailure { Log.e(TAG, "loadTicket() failed", it) }
                .getOrNull()

            if (ticket != null) {
                _uiState.value = _uiState.value.copy(
                    ticket = ticket,
                    title = ticket.showName,
                    seatLabel = ticket.seatLabel,
                )
            }
        }
    }

    companion object {
        private const val TAG = "QrCheckinViewModel"

        fun factory(ticketId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                QrCheckinViewModel(
                    ticketId = ticketId,
                    ticketRepository = app.appContainer.ticketRepository,
                    ticketService = app.appContainer.ticketService,
                )
            }
        }
    }
}
