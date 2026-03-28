package com.ssafy.cheket.features.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.TransactionDto
import com.ssafy.cheket.core.network.service.WalletService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TxTypeFilter(val label: String, val apiValue: String?) {
    ALL("전체", null),
    PURCHASE("구매", "PURCHASE"),
    RESALE_PURCHASE("2차거래 정산", "RESALE_PURCHASE"),
    RESALE_CREATE("2차거래 등록", "RESALE_CREATE"),
    RESALE_CANCEL("2차거래 취소", "RESALE_CANCEL"),
    TRANSFER("양도", "TRANSFER"),
    REFUND("환불", "REFUND"),
    CHARGE("충전", "CHARGE"),
    SETTLEMENT("정산", "SETTLEMENT"),
}

data class TxUiItem(
    val id: Long,
    val type: String,
    val typeLabel: String,
    val description: String,
    val amount: Long,
    val displayAmount: Long?,      // FAILED면 null, 아니면 resolvedAmount
    val txStatus: String? = null,  // PENDING, SUBMITTED, CONFIRMED, FAILED
    val txHash: String? = null,
    val blockNumber: Long? = null,
    val createdAt: String,
    val dateLabel: String,
    val timeLabel: String,
)

sealed class WalletHistoryUiState {
    data object Loading : WalletHistoryUiState()
    data class Error(val message: String) : WalletHistoryUiState()
    data class Success(
        val grouped: Map<String, List<TxUiItem>>,
    ) : WalletHistoryUiState()
}

class WalletHistoryViewModel(
    private val walletService: WalletService,
    private val currentUserId: Long?,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WalletHistoryUiState>(WalletHistoryUiState.Loading)
    val uiState: StateFlow<WalletHistoryUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TxTypeFilter.ALL)
    val selectedFilter: StateFlow<TxTypeFilter> = _selectedFilter.asStateFlow()

    init {
        load()
    }

    fun onFilterChange(filter: TxTypeFilter) {
        if (_selectedFilter.value == filter) return
        _selectedFilter.value = filter
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = WalletHistoryUiState.Loading
            try {
                val response = walletService.getTransactions(
                    type = _selectedFilter.value.apiValue,
                )
                val dtos = response.data ?: emptyList()

                Log.d(TAG, "load() filter=${_selectedFilter.value}, transactions=${dtos.size}, currentUserId=$currentUserId")

                if (dtos.isEmpty()) {
                    _uiState.value = WalletHistoryUiState.Success(grouped = emptyMap())
                    return@launch
                }

                val items = dtos.map { it.toUiItem(currentUserId) }
                    .sortedByDescending { it.createdAt }

                val grouped = items.groupBy { it.dateLabel }

                _uiState.value = WalletHistoryUiState.Success(grouped = grouped)
            } catch (e: Exception) {
                Log.e(TAG, "load() error", e)
                _uiState.value = WalletHistoryUiState.Error(
                    "트랜젝션 내역을 불러오지 못했습니다: ${e.message}"
                )
            }
        }
    }

    companion object {
        private const val TAG = "WalletHistoryVM"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                WalletHistoryViewModel(
                    walletService = app.appContainer.walletService,
                    currentUserId = app.authDataStore.getUserId(),
                )
            }
        }
    }
}

private val TYPE_LABELS = mapOf(
    "CHARGE" to "충전",
    "PURCHASE" to "구매",
    // NOTE: RESALE_PURCHASE는 buyer/seller 구분 없이 동일 타입으로 내려옴.
    // buyerId == 나이면 구매(지출), sellerId == 나이면 판매 수익(수입).
    // 양쪽 모두 같은 레코드를 조회하므로 "2차거래 정산"으로 통일 표기.
    "RESALE_PURCHASE" to "2차거래 정산",
    "RESALE_CREATE" to "2차거래 등록",
    "RESALE_CANCEL" to "2차거래 취소",
    "TRANSFER" to "양도",
    "REFUND" to "환불",
    "SETTLEMENT" to "정산",
    "STAKEHOLDER_MINT" to "공연 등록",
)

private fun TransactionDto.toUiItem(currentUserId: Long?): TxUiItem {
    val dateLabel = try {
        createdAt.take(10).replace("-", ".")
    } catch (_: Exception) { "" }

    val timeLabel = try {
        createdAt.substring(11, 16)
    } catch (_: Exception) { "" }

    val isFailed = txStatus == "FAILED"

    val resolvedAmount = if (currentUserId != null && (buyerId != null || sellerId != null)) {
        val absAmount = kotlin.math.abs(amount)
        when {
            type == "CHARGE" || type == "REFUND" || type == "SETTLEMENT" -> absAmount
            type == "RESALE_CREATE" || type == "RESALE_CANCEL" || type == "STAKEHOLDER_MINT" -> 0L
            type == "TRANSFER" && amount > 0 && sellerId == null -> absAmount // 초기 SSF 전송
            type == "TRANSFER" -> 0L
            buyerId == currentUserId -> -absAmount
            sellerId == currentUserId -> absAmount
            else -> amount
        }
    } else {
        amount
    }

    val displayAmount = if (isFailed) null else resolvedAmount

    return TxUiItem(
        id = transactionId,
        type = type,
        typeLabel = TYPE_LABELS[type] ?: type,
        description = description ?: TYPE_LABELS[type] ?: type,
        amount = resolvedAmount,
        displayAmount = displayAmount,
        txStatus = txStatus,
        txHash = txHash,
        blockNumber = blockNumber,
        createdAt = createdAt,
        dateLabel = dateLabel,
        timeLabel = timeLabel,
    )
}
