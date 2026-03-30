package com.ssafy.cheket.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.repository.ShowRepository
import com.ssafy.cheket.core.repository.ResaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val bannerSlides: List<BannerSlide> = emptyList(),
    val categories: List<CategoryIcon> = emptyList(),
    val rankingItems: List<RankingItem> = emptyList(),
    val openSchedule: List<OpenScheduleItem> = emptyList(),
    val resaleItems: List<ResaleItem> = emptyList(),
    val likedShows: List<LikedShow> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val showRepository: ShowRepository,
    private val resaleRepository: ResaleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "init — loading home data")
        load()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        load()
    }

    private fun load() {
        // 1. 배너 (추천 API 시도 후 실패 시 랭킹 fallback)
        viewModelScope.launch {
            showRepository.getBannerSlides()
                .catch { e -> Log.e(TAG, "getBannerSlides() error", e) }
                .collect {
                    Log.d(TAG, "getBannerSlides() received ${it.size} items")
                    _uiState.value = _uiState.value.copy(bannerSlides = it)
                }
        }

        // 2. 랭킹 (인기순 top 5)
        viewModelScope.launch {
            showRepository.getRankingItems()
                .catch { e -> Log.e(TAG, "getRankingItems() error", e) }
                .collect {
                    Log.d(TAG, "getRankingItems() received ${it.size} items")
                    _uiState.value = _uiState.value.copy(rankingItems = it)
                }
        }

        // 3. 오픈 예정
        viewModelScope.launch {
            showRepository.getOpenSchedule()
                .catch { e -> Log.e(TAG, "getOpenSchedule() error", e) }
                .collect {
                    Log.d(TAG, "getOpenSchedule() received ${it.size} items")
                    _uiState.value = _uiState.value.copy(openSchedule = it)
                }
        }

        // 4. 찜한 공연
        viewModelScope.launch {
            try {
                val liked = showRepository.getLikedShows()
                Log.d(TAG, "getLikedShows() received ${liked.size} items")
                _uiState.value = _uiState.value.copy(likedShows = liked)
            } catch (e: Exception) {
                Log.e(TAG, "getLikedShows() error", e)
            }
        }

        // 5. 리세일 할인
        viewModelScope.launch {
            resaleRepository.getResaleItems()
                .catch { e ->
                    Log.e(TAG, "getResaleItems() error", e)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect {
                    Log.d(TAG, "getResaleItems() received ${it.size} items")
                    _uiState.value = _uiState.value.copy(resaleItems = it, isLoading = false)
                }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                HomeViewModel(app.appContainer.showRepository, app.appContainer.resaleRepository)
            }
        }
    }
}
