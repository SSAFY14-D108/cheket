package com.ssafy.cheket.features.show

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.repository.ShowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShowDetailViewModel(
    private val showId: String,
    private val showRepository: ShowRepository,
) : ViewModel() {

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val show: Show, val isLiked: Boolean, val likeCount: Int) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 좋아요 디바운싱: 빠른 연타 시 마지막 상태만 API 호출
    private var likeJob: Job? = null

    init {
        loadShow()
    }

    fun loadShow() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val show = showRepository.getShowById(showId)
                if (show != null) {
                    _uiState.value = UiState.Success(
                        show = show,
                        isLiked = show.isLiked,
                        likeCount = show.likeCount,
                    )
                    Log.d(TAG, "loadShow() success: ${show.name}, isLiked=${show.isLiked}, likeCount=${show.likeCount}")
                } else {
                    _uiState.value = UiState.Error("공연을 찾을 수 없습니다.")
                    Log.w(TAG, "loadShow() show not found: $showId")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
                Log.e(TAG, "loadShow() error", e)
            }
        }
    }

    /** 찜 토글 — UI 즉시 반영 + 500ms 디바운싱 후 API 호출 */
    fun toggleLike() {
        val current = _uiState.value as? UiState.Success ?: return
        val newLiked = !current.isLiked
        val newCount = current.likeCount + if (newLiked) 1 else -1

        // UI 즉시 갱신 (optimistic update)
        _uiState.value = current.copy(isLiked = newLiked, likeCount = newCount.coerceAtLeast(0))
        Log.d(TAG, "toggleLike() optimistic: isLiked=$newLiked, likeCount=$newCount")

        // 디바운싱: 연타 시 이전 API 호출 취소, 500ms 후 최종 상태만 전송
        likeJob?.cancel()
        likeJob = viewModelScope.launch {
            delay(500)
            try {
                if (newLiked) {
                    showRepository.likeShow(showId)
                } else {
                    showRepository.unlikeShow(showId)
                }
                Log.d(TAG, "toggleLike() API success: isLiked=$newLiked")
            } catch (e: Exception) {
                // API 실패 시 롤백
                Log.e(TAG, "toggleLike() API error, rolling back", e)
                _uiState.value = current
            }
        }
    }

    companion object {
        private const val TAG = "ShowDetailViewModel"

        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ShowDetailViewModel(
                    showId = showId,
                    showRepository = app.appContainer.showRepository,
                )
            }
        }
    }
}
