package com.ssafy.cheket.features.collection

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
import kotlinx.coroutines.launch

data class CollectionUiState(
    val usedTickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
)

class CollectionViewModel(private val ticketRepository: TicketRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init — loading used tickets")
        viewModelScope.launch {
            ticketRepository.getUsedTickets().collect { tickets ->
                Log.d(TAG, "getUsedTickets() received ${tickets.size} tickets")
                _uiState.value = CollectionUiState(usedTickets = tickets, isLoading = false)
            }
        }
    }

    companion object {
        private const val TAG = "CollectionViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                CollectionViewModel(app.appContainer.ticketRepository)
            }
        }
    }
}
