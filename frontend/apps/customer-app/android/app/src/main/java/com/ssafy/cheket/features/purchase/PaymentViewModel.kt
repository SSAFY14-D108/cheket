package com.ssafy.cheket.features.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.navigation.NavParams
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentStep { REVIEW, APPROVED, SUCCESS, FAILURE }

data class PaymentUiState(
    val event: Event? = null,
    val selectedSeats: List<Seat> = emptyList(),
    val totalPrice: Int = 0,
    val user: User = MockDataSource.mockUser,
    val step: PaymentStep = PaymentStep.REVIEW,
    val isProcessing: Boolean = false,
    val simulateFailure: Boolean = false,
    val failureReason: String = "",
)

class PaymentViewModel(
    private val eventId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        val event = MockDataSource.mockEvents.find { it.id == eventId }
        _uiState.value = PaymentUiState(
            event = event,
            selectedSeats = NavParams.selectedSeats,
            totalPrice = NavParams.totalPrice,
            user = MockDataSource.mockUser,
        )
    }

    fun toggleFailureSimulation() {
        _uiState.update { it.copy(simulateFailure = !it.simulateFailure) }
    }

    fun approve() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            delay(1200) // Simulate blockchain approval
            _uiState.update {
                it.copy(
                    step = PaymentStep.APPROVED,
                    isProcessing = false,
                )
            }
        }
    }

    fun confirmPurchase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            delay(1500) // Simulate purchase confirmation

            val state = _uiState.value
            if (state.simulateFailure) {
                val reason = "INSUFFICIENT_BALANCE"
                NavParams.failureReason = reason
                _uiState.update {
                    it.copy(
                        step = PaymentStep.FAILURE,
                        isProcessing = false,
                        failureReason = reason,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        step = PaymentStep.SUCCESS,
                        isProcessing = false,
                    )
                }
            }
        }
    }

    companion object {
        fun factory(eventId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PaymentViewModel(eventId)
            }
        }
    }
}
