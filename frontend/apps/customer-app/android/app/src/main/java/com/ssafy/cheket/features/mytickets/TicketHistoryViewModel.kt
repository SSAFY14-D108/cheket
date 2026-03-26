package com.ssafy.cheket.features.mytickets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class TicketHistoryTab(val label: String) {
    UPCOMING("예정/보유"),
    USED("사용완료"),
}

data class TicketHistoryUiState(
    val upcomingTickets: List<Ticket> = emptyList(),
    val usedTickets: List<Ticket> = emptyList(),
    val selectedTab: TicketHistoryTab = TicketHistoryTab.UPCOMING,
    val isLoading: Boolean = true,
    val upcomingFailed: Boolean = false,
    val usedFailed: Boolean = false,
    val errorMessage: String? = null,
) {
    val visibleTickets: List<Ticket>
        get() = when (selectedTab) {
            TicketHistoryTab.UPCOMING -> upcomingTickets
            TicketHistoryTab.USED -> usedTickets
        }

    /** Only show partial error when the currently visible tab's data failed to load */
    val visibleErrorMessage: String?
        get() = when {
            selectedTab == TicketHistoryTab.UPCOMING && upcomingFailed -> "예정/보유 티켓을 불러오지 못했어요."
            selectedTab == TicketHistoryTab.USED && usedFailed -> "사용완료 티켓을 불러오지 못했어요."
            else -> null
        }
}

class TicketHistoryViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketHistoryUiState())
    val uiState: StateFlow<TicketHistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val upcomingResult = runCatching { ticketRepository.getTickets().first() }
            val usedResult = runCatching { ticketRepository.getUsedTickets().first() }

            val upcoming = upcomingResult.getOrElse {
                Log.e(TAG, "refresh() upcoming failed", it)
                emptyList()
            }
            val used = usedResult.getOrElse {
                Log.e(TAG, "refresh() used failed", it)
                emptyList()
            }

            val hasAnySuccess = upcomingResult.isSuccess || usedResult.isSuccess

            if (hasAnySuccess) {
                Log.d(TAG, "refresh() upcoming=${upcoming.size}, used=${used.size}")
                _uiState.value = _uiState.value.copy(
                    upcomingTickets = upcoming,
                    usedTickets = used,
                    isLoading = false,
                    upcomingFailed = upcomingResult.isFailure,
                    usedFailed = usedResult.isFailure,
                    errorMessage = null,
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    upcomingTickets = emptyList(),
                    usedTickets = emptyList(),
                    isLoading = false,
                    upcomingFailed = true,
                    usedFailed = true,
                    errorMessage = "티켓 내역을 불러오지 못했어요.",
                )
            }
        }
    }

    fun selectTab(tab: TicketHistoryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    companion object {
        private const val TAG = "TicketHistoryViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                TicketHistoryViewModel(app.appContainer.ticketRepository)
            }
        }
    }
}
