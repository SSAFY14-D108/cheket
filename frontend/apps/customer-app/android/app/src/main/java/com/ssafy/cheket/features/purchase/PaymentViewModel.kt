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
import com.ssafy.cheket.core.network.dto.PurchaseRequest
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.network.service.TicketService
import com.ssafy.cheket.core.network.service.WalletService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentStep { REVIEW, PROCESSING, SUCCESS, FAILURE }

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
    val txId: Long? = null,
)

class PaymentViewModel(
    private val showId: String,
    private val showService: ShowService,
    private val ticketService: TicketService,
    private val walletService: WalletService,
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
                            status = when (detail.status) {
                                "UPCOMING" -> ShowStatus.UPCOMING
                                "SOLD_OUT" -> ShowStatus.SOLD_OUT
                                "COMPLETED" -> ShowStatus.COMPLETED
                                else -> ShowStatus.ON_SALE
                            },
                            maxPerUser = 4,
                            grades = detail.grade.map { g ->
                                com.ssafy.cheket.core.model.Grade(
                                    name = g.gradeName,
                                    price = g.price,
                                    color = g.colorCode,
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

            // SSF 잔액 조회
            var ctkBalance = 0
            try {
                val balanceResponse = walletService.refreshBalance()
                ctkBalance = balanceResponse.data?.balance ?: 0
                Log.d(TAG, "load() — SSF balance: $ctkBalance")
            } catch (e: Exception) {
                Log.e(TAG, "load() — failed to fetch balance", e)
            }

            _uiState.value = PaymentUiState(
                show = show,
                selectedSeats = seats,
                totalPrice = totalPrice,
                user = User(
                    id = "", name = "", email = "", phone = "",
                    ctkBalance = ctkBalance, walletAddress = "",
                ),
                isLoading = false,
            )
        }
    }

    fun toggleFailureSimulation() {
        val newValue = !_uiState.value.simulateFailure
        Log.d(TAG, "toggleFailureSimulation() simulateFailure=$newValue")
        _uiState.update { it.copy(simulateFailure = newValue) }
    }

    fun purchase() {
        Log.d(TAG, "purchase() — calling purchase API")
        viewModelScope.launch {
            _uiState.update { it.copy(step = PaymentStep.PROCESSING, isProcessing = true) }

            try {
                val showIdLong = showId.toLong()
                val sessionId = NavParams.sessionId
                val seatAccessToken = NavParams.seatAccessToken
                    ?: throw IllegalStateException("Seat-Access-Token이 없습니다. 대기열을 통해 진입해주세요.")
                val seatIds = NavParams.selectedSeats.map { it.id.toLong() }

                Log.d(TAG, "purchase() — showId=$showIdLong, sessionId=$sessionId, seatIds=$seatIds, token=${seatAccessToken.take(10)}...")

                val response = ticketService.purchaseTicket(
                    seatAccessToken = seatAccessToken,
                    showId = showIdLong,
                    sessionId = sessionId,
                    request = PurchaseRequest(sessionSeatIds = seatIds),
                )

                val txId = response.data?.txId
                Log.d(TAG, "purchase() — API success, txId=$txId")

                _uiState.update {
                    it.copy(
                        step = PaymentStep.SUCCESS,
                        isProcessing = false,
                        txId = txId,
                    )
                }
            } catch (e: Exception) {
                val reason = e.message ?: "구매 처리 중 오류가 발생했습니다"
                Log.e(TAG, "purchase() — API failed", e)
                NavParams.failureReason = reason
                _uiState.update {
                    it.copy(
                        step = PaymentStep.FAILURE,
                        isProcessing = false,
                        failureReason = reason,
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
                    ticketService = app.appContainer.ticketService,
                    walletService = app.appContainer.walletService,
                )
            }
        }
    }
}
