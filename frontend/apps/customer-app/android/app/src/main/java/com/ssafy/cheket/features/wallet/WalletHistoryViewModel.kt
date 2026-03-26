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
import java.text.SimpleDateFormat
import java.util.Locale

data class TxUiItem(
    val id: Long,
    val type: String,
    val typeLabel: String,
    val description: String,
    val amount: Long,
    val txStatus: String? = null, // PENDING, SUBMITTED, CONFIRMED, FAILED
    val createdAt: String,      // ISO "2026-03-15T14:30:00"
    val dateLabel: String,      // "2026.03.15"
    val timeLabel: String,      // "14:30"
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

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = WalletHistoryUiState.Loading
            try {
                val response = walletService.getTransactions()
                val dtos = response.data ?: emptyList()

                Log.d(TAG, "load() transactions=${dtos.size}, currentUserId=$currentUserId")

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
                    "거래 내역을 불러오지 못했습니다: ${e.message}"
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
    "RESALE_BUY" to "리세일 구매",
    "RESALE_SELL" to "리세일 판매",
    "TRANSFER_SEND" to "양도 전송",
    "TRANSFER_RECEIVE" to "양도 수신",
    "REFUND" to "환불",
)

private fun TransactionDto.toUiItem(currentUserId: Long?): TxUiItem {
    // createdAt: "2026-03-15T14:30:00" 형태
    val dateLabel = try {
        createdAt.take(10).replace("-", ".")
    } catch (_: Exception) { "" }

    val timeLabel = try {
        createdAt.substring(11, 16)
    } catch (_: Exception) { "" }

    // Determine sign based on buyerId/sellerId when userId is available
    val resolvedAmount = if (currentUserId != null && (buyerId != null || sellerId != null)) {
        val absAmount = kotlin.math.abs(amount)
        when {
            type == "CHARGE" || type == "REFUND" -> absAmount  // always income
            buyerId == currentUserId -> -absAmount              // expense (I am buyer)
            sellerId == currentUserId -> absAmount              // income (I am seller)
            else -> amount                                      // fallback to raw sign
        }
    } else {
        amount
    }

    return TxUiItem(
        id = transactionId,
        type = type,
        typeLabel = TYPE_LABELS[type] ?: type,
        description = description ?: TYPE_LABELS[type] ?: type,
        amount = resolvedAmount,
        txStatus = txStatus,
        createdAt = createdAt,
        dateLabel = dateLabel,
        timeLabel = timeLabel,
    )
}
