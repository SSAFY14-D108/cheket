package com.ssafy.cheket.features.wishlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.LikedShowDto
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.network.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WishlistUiState(
    val shows: List<LikedShowDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class WishlistViewModel(
    private val userService: UserService,
    private val showService: ShowService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState())
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = userService.getLikedShows()
                Log.d(TAG, "getLikedShows() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
                _uiState.value = _uiState.value.copy(
                    shows = response.data ?: emptyList(),
                    isLoading = false,
                )
            } catch (e: Exception) {
                Log.e(TAG, "getLikedShows() error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "찜 목록을 불러올 수 없습니다.",
                )
            }
        }
    }

    fun unlikeShow(showId: Long) {
        viewModelScope.launch {
            try {
                showService.unlikeShow(showId)
                Log.d(TAG, "unlikeShow() showId=$showId success")
                // 로컬에서 즉시 제거
                _uiState.value = _uiState.value.copy(
                    shows = _uiState.value.shows.filter { it.showId != showId },
                )
            } catch (e: Exception) {
                Log.e(TAG, "unlikeShow() error", e)
            }
        }
    }

    companion object {
        private const val TAG = "WishlistVM"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                WishlistViewModel(
                    app.appContainer.userService,
                    app.appContainer.showService,
                )
            }
        }
    }
}
