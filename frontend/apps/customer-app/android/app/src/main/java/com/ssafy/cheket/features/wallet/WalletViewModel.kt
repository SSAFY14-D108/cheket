package com.ssafy.cheket.features.wallet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.service.WalletService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletUiState(
    val isLoading: Boolean = true,
    val balance: Int = 0,
    val walletAddress: String = "",
    val error: String? = null,
)

class WalletViewModel(
    private val walletService: WalletService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = walletService.refreshBalance()
                val data = response.data
                _uiState.value = WalletUiState(
                    isLoading = false,
                    balance = data?.balance ?: 0,
                    walletAddress = data?.walletAddress ?: "",
                )
            } catch (e: Exception) {
                Log.e(TAG, "load() error", e)
                _uiState.value = WalletUiState(
                    isLoading = false,
                    error = "지갑 정보를 불러오지 못했어요.",
                )
            }
        }
    }

    companion object {
        private const val TAG = "WalletViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                WalletViewModel(app.appContainer.walletService)
            }
        }
    }
}
