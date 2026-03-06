package com.ssafy.cheket.features.mytickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TicketFilter(val label: String) {
    ALL("전체"), SOLD("보유중"), LISTED("판매중"), USED("사용됨"), EXPIRED("만료됨")
}

data class MyTicketsUiState(
    val allTickets: List<Ticket> = emptyList(),
    val filteredTickets: List<Ticket> = emptyList(),
    val selectedFilter: TicketFilter = TicketFilter.ALL,
    val isLoading: Boolean = true,
)

class MyTicketsViewModel(private val ticketRepository: TicketRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MyTicketsUiState())
    val uiState: StateFlow<MyTicketsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ticketRepository.getTickets().collect { tickets ->
                _uiState.value = _uiState.value.copy(allTickets = tickets, isLoading = false)
                applyFilter()
            }
        }
    }

    fun onFilterChange(filter: TicketFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilter()
    }

    private fun applyFilter() {
        val s = _uiState.value
        val result = when (s.selectedFilter) {
            TicketFilter.ALL -> s.allTickets
            TicketFilter.SOLD -> s.allTickets.filter { it.status == TicketStatus.SOLD }
            TicketFilter.LISTED -> s.allTickets.filter { it.status == TicketStatus.LISTED }
            TicketFilter.USED -> s.allTickets.filter { it.status == TicketStatus.USED }
            TicketFilter.EXPIRED -> s.allTickets.filter { it.status == TicketStatus.EXPIRED }
        }
        _uiState.value = s.copy(filteredTickets = result)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                MyTicketsViewModel(app.appContainer.ticketRepository)
            }
        }
    }
}
