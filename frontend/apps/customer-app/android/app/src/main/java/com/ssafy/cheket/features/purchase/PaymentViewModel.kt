package com.ssafy.cheket.features.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.Show
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
    val show: Show? = null,
    val selectedSeats: List<Seat> = emptyList(),
    val totalPrice: Int = 0,
    val user: User = MockDataSource.mockUser,
    val step: PaymentStep = PaymentStep.REVIEW,
    val isProcessing: Boolean = false,
    val simulateFailure: Boolean = false,
    val failureReason: String = "",
)

class PaymentViewModel(
    private val showId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init — showId=$showId")
        val show = MockDataSource.mockShows.find { it.id == showId }
        Log.d(TAG, "init — show found: ${show != null}, seats=${NavParams.selectedSeats.size}, totalPrice=${NavParams.totalPrice}")
        _uiState.value = PaymentUiState(
            show = show,
            selectedSeats = NavParams.selectedSeats,
            totalPrice = NavParams.totalPrice,
            user = MockDataSource.mockUser,
        )
    }

    fun toggleFailureSimulation() {
        val newValue = !_uiState.value.simulateFailure
        Log.d(TAG, "toggleFailureSimulation() simulateFailure=$newValue")
        _uiState.update { it.copy(simulateFailure = newValue) }
    }

    fun approve() {
        Log.d(TAG, "approve() — starting blockchain approval")
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            delay(1200) // Simulate blockchain approval
            Log.d(TAG, "approve() — approval complete")
            _uiState.update {
                it.copy(
                    step = PaymentStep.APPROVED,
                    isProcessing = false,
                )
            }
        }
    }

    fun confirmPurchase() {
        Log.d(TAG, "confirmPurchase() — starting purchase confirmation")
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            delay(1500) // Simulate purchase confirmation

            val state = _uiState.value
            if (state.simulateFailure) {
                val reason = "INSUFFICIENT_BALANCE"
                Log.w(TAG, "confirmPurchase() — simulated failure: $reason")
                NavParams.failureReason = reason
                _uiState.update {
                    it.copy(
                        step = PaymentStep.FAILURE,
                        isProcessing = false,
                        failureReason = reason,
                    )
                }
            } else {
                Log.d(TAG, "confirmPurchase() — purchase success")
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
        private const val TAG = "PaymentViewModel"

        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PaymentViewModel(showId)
            }
        }
    }
}
