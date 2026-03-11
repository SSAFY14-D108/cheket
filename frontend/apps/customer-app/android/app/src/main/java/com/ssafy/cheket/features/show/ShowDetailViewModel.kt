package com.ssafy.cheket.features.show

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.repository.ShowRepository
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
        data class Success(val show: Show) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadShow()
    }

    fun loadShow() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val show = showRepository.getShowById(showId)
                if (show != null) {
                    _uiState.value = UiState.Success(show)
                    Log.d(TAG, "loadShow() success: ${show.name}")
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
