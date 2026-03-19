package com.ssafy.cheket.features.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.User
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.network.service.ShowService
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
    val user: User = User(
        id = "", name = "", email = "", phone = "",
        ctkBalance = 0, walletAddress = "",
    ),
    val step: PaymentStep = PaymentStep.REVIEW,
    val isProcessing: Boolean = false,
    val simulateFailure: Boolean = false,
    val failureReason: String = "",
    val isLoading: Boolean = true,
)

class PaymentViewModel(
    private val showId: String,
    private val showService: ShowService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init — showId=$showId")
        load()
    }

    private fun load() {
        viewModelScope.launch {
            // NavParams에서 좌석/가격/공연 정보 가져오기
            val seats = NavParams.selectedSeats
            val totalPrice = NavParams.totalPrice
            var show = NavParams.showInfo

            Log.d(TAG, "load() — NavParams show=${show?.name}, seats=${seats.size}, totalPrice=$totalPrice")

            // show가 없으면 API에서 가져오기
            if (show == null) {
                try {
                    val showIdLong = showId.toLong()
                    val response = showService.getShowDetail(showIdLong)
                    val detail = response.data
                    if (detail != null) {
                        show = Show(
                            id = detail.showId.toString(),
                            name = detail.title,
                            artistName = detail.artist,
                            date = detail.show?.showStartDate ?: "",
                            venue = detail.venue,
                            region = detail.region,
                            poster = detail.posterUrl,
                            status = if (detail.status == "SOLD_OUT") ShowStatus.SOLD_OUT
                            else ShowStatus.ON_SALE,
                            maxPerUser = 4,
                            grades = detail.grade.map { g ->
                                com.ssafy.cheket.core.model.Grade(
                                    name = g.gradeName,
                                    price = g.price,
                                    remaining = 0,
                                )
                            },
                            description = detail.description,
                            isLiked = detail.isLiked,
                            likeCount = detail.likeCount,
                        )
                        Log.d(TAG, "load() — fetched show from API: ${show.name}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "load() — failed to fetch show detail", e)
                }
            }

            _uiState.value = PaymentUiState(
                show = show,
                selectedSeats = seats,
                totalPrice = totalPrice,
                isLoading = false,
            )
        }
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
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                PaymentViewModel(
                    showId = showId,
                    showService = app.appContainer.showService,
                )
            }
        }
    }
}
