package com.ssafy.cheket.features.resale

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.ResaleTicketDto
import com.ssafy.cheket.core.network.service.ResaleService
import com.ssafy.cheket.core.network.service.ShowService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResaleTicketUiItem(
    val ticketId: Long,
    val seatLabel: String,    // "A구역 3열 12번"
    val grade: String,
    val showDate: String,     // "2026.04.12 (토) 18:00"
    val originalPrice: Int,
    val resalePrice: Int,
    val discountRate: Double,
)

data class ResaleShowInfo(
    val showId: String,
    val title: String,
    val posterUrl: String,
    val venue: String,
    val showDate: String = "",  // "2026.05.03 ~ 2026.05.05"
)

sealed class ResaleTicketsUiState {
    data object Loading : ResaleTicketsUiState()
    data class Error(val message: String) : ResaleTicketsUiState()
    data class Success(
        val showInfo: ResaleShowInfo,
        val tickets: List<ResaleTicketUiItem>,
        val totalCount: Int,
    ) : ResaleTicketsUiState()
}

class ResaleTicketsViewModel(
    private val showId: String,
    private val resaleService: ResaleService,
    private val showService: ShowService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResaleTicketsUiState>(ResaleTicketsUiState.Loading)
    val uiState: StateFlow<ResaleTicketsUiState> = _uiState.asStateFlow()

    // 공연 정보 캐시 (sort 변경 시 재호출 방지)
    private var cachedShowInfo: ResaleShowInfo? = null

    init {
        load()
    }

    fun load(sort: String? = null) {
        viewModelScope.launch {
            _uiState.value = ResaleTicketsUiState.Loading
            try {
                val showIdLong = showId.toLong()

                // 공연 정보 (캐시 없으면 가져오기)
                val showInfo = cachedShowInfo ?: run {
                    val showResponse = showService.getShowDetail(showIdLong)
                    val detail = showResponse.data
                    val startDate = detail?.show?.showStartDate ?: ""
                    val endDate = detail?.show?.showEndDate ?: ""
                    val dateStr = if (startDate.isNotBlank() && endDate.isNotBlank() && startDate != endDate) {
                        "${startDate.replace("-", ".")} ~ ${endDate.replace("-", ".")}"
                    } else if (startDate.isNotBlank()) {
                        startDate.replace("-", ".")
                    } else ""

                    ResaleShowInfo(
                        showId = showId,
                        title = detail?.title ?: "공연",
                        posterUrl = detail?.posterUrl ?: "",
                        venue = detail?.venue ?: "",
                        showDate = dateStr,
                    ).also { cachedShowInfo = it }
                }

                // 2차 거래 티켓 목록
                val response = resaleService.getResaleTickets(showIdLong, sort = sort)
                val data = response.data

                if (data == null) {
                    _uiState.value = ResaleTicketsUiState.Error("데이터를 불러오지 못했습니다")
                    return@launch
                }

                Log.d(TAG, "load() show=${showInfo.title}, tickets=${data.tickets.size}")

                val tickets = data.tickets.map { it.toUiItem() }

                _uiState.value = ResaleTicketsUiState.Success(
                    showInfo = showInfo,
                    tickets = tickets,
                    totalCount = data.totalElements,
                )
            } catch (e: Exception) {
                Log.e(TAG, "load() error", e)
                _uiState.value = ResaleTicketsUiState.Error(
                    "티켓 목록을 불러오지 못했습니다: ${e.message}"
                )
            }
        }
    }

    /** 2차 거래 티켓 구매 */
    fun purchaseResale(ticketId: Long, onSuccess: (txId: Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "purchaseResale() ticketId=$ticketId")
                val response = resaleService.purchaseResale(ticketId)
                val txId = response.data?.txId
                if (txId != null) {
                    Log.d(TAG, "purchaseResale() success, txId=$txId")
                    onSuccess(txId)
                } else {
                    Log.e(TAG, "purchaseResale() no txId in response")
                    onError("구매 응답에 txId가 없습니다.")
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val msg = try {
                    org.json.JSONObject(errorBody ?: "").optString("errorMessage", "구매에 실패했습니다.")
                } catch (_: Exception) { "구매에 실패했습니다. (${e.code()})" }
                Log.e(TAG, "purchaseResale() failed: $msg", e)
                onError(msg)
            } catch (e: Exception) {
                Log.e(TAG, "purchaseResale() failed", e)
                onError(e.message ?: "구매 처리 중 오류가 발생했습니다.")
            }
        }
    }

    companion object {
        private const val TAG = "ResaleTicketsVM"

        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ResaleTicketsViewModel(
                    showId = showId,
                    resaleService = app.appContainer.resaleService,
                    showService = app.appContainer.showService,
                )
            }
        }
    }
}

private fun ResaleTicketDto.toUiItem(): ResaleTicketUiItem {
    val datePart = session.sessionDate.replace("-", ".")
    val timePart = session.sessionStartTime.take(5)
    val dow = try {
        val parts = session.sessionDate.split("-")
        val cal = java.util.Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        }
        val weekdays = arrayOf("일", "월", "화", "수", "목", "금", "토")
        weekdays[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    } catch (_: Exception) { "" }

    return ResaleTicketUiItem(
        ticketId = ticketId,
        seatLabel = "$sectionName $seatNo",
        grade = grade,
        showDate = "$datePart ($dow) $timePart",
        originalPrice = originalPrice,
        resalePrice = discountedPrice,
        discountRate = discountRate,
    )
}
