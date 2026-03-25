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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QrCheckinUiState(
    val ticket: Ticket? = null,
    val title: String = "",
    val seatLabel: String = "",
    val qrData: String? = null,       // QR 코드에 담을 JWT 토큰
    val expiresIn: Int = 0,           // 남은 초
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
    private var countdownJob: Job? = null

    init {
        loadTicket()
        refreshQr()
    }

    fun refreshQr() {
        val ticketIdLong = ticketId.toLongOrNull()
        if (ticketIdLong == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false, isRefreshing = false,
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

            val result = safeCall { ticketService.generateQrToken(ticketIdLong) }

            result
                .onSuccess { response ->
                    val data = response.data
                    Log.d(TAG, "refreshQr() statusCode=${response.httpStatusCode}, expiresIn=${data?.expiresIn}")

                    if (response.httpStatusCode in 200..299 && data != null) {
                        _uiState.value = _uiState.value.copy(
                            qrData = data.qrToken,
                            expiresIn = data.expiresIn,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                        startCountdown(data.expiresIn)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false, isRefreshing = false,
                            errorMessage = response.responseMessage ?: "QR 코드를 불러오지 못했습니다.",
                        )
                    }
                }
                .onFailure { throwable ->
                    val msg = throwable.message ?: "QR 코드를 불러오지 못했습니다."
                    Log.e(TAG, "refreshQr() failed: $msg", throwable)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, isRefreshing = false,
                        errorMessage = msg,
                    )
                }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(expiresIn = remaining)
                delay(1000)
                remaining--
            }
            // 만료 → 자동 갱신
            _uiState.value = _uiState.value.copy(expiresIn = 0)
            Log.d(TAG, "QR expired, auto-refreshing")
            refreshQr()
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

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
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
