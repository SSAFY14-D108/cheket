package com.ssafy.cheket.features.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.network.service.WalletService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyPageUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val walletAddress: String = "",
    val ctkBalance: Int = 0,
    val wishlistCount: Int = 0,
    val error: String? = null,
)

class MyPageViewModel(
    private val userService: UserService,
    private val walletService: WalletService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = MyPageUiState(isLoading = true)
            try {
                val userResponse = userService.getUserInfo()
                val user = userResponse.data

                val walletResponse = walletService.refreshBalance()
                val wallet = walletResponse.data

                val likesResponse = userService.getLikedShows()
                val likesCount = likesResponse.data?.size ?: 0

                Log.d(TAG, "load() user=${user?.username}, balance=${wallet?.balance}, likes=$likesCount")

                _uiState.value = MyPageUiState(
                    isLoading = false,
                    name = user?.username ?: "",
                    phone = user?.phoneNumber ?: "",
                    email = user?.email ?: "",
                    walletAddress = wallet?.walletAddress ?: "",
                    ctkBalance = wallet?.balance ?: 0,
                    wishlistCount = likesCount,
                )
            } catch (e: Exception) {
                Log.e(TAG, "load() error", e)
                _uiState.value = MyPageUiState(
                    isLoading = false,
                    error = "마이페이지 정보를 불러오지 못했습니다.",
                )
            }
        }
    }

    companion object {
        private const val TAG = "MyPageViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                MyPageViewModel(
                    userService = app.appContainer.userService,
                    walletService = app.appContainer.walletService,
                )
            }
        }
    }
}
