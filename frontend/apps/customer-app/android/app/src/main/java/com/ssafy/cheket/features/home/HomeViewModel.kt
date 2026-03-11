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
import kotlinx.coroutines.launch

data class HomeUiState(
    val bannerSlides: List<BannerSlide> = emptyList(),
    val categories: List<CategoryIcon> = emptyList(),
    val rankingItems: List<RankingItem> = emptyList(),
    val openSchedule: List<OpenScheduleItem> = emptyList(),
    val resaleItems: List<ResaleItem> = emptyList(),
    val shows: List<Show> = emptyList(),
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

    private fun load() {
        viewModelScope.launch {
            showRepository.getBannerSlides().collect {
                Log.d(TAG, "getBannerSlides() received ${it.size} slides")
                _uiState.value = _uiState.value.copy(bannerSlides = it)
            }
        }
        viewModelScope.launch {
            showRepository.getCategories().collect {
                Log.d(TAG, "getCategories() received ${it.size} categories")
                _uiState.value = _uiState.value.copy(categories = it)
            }
        }
        viewModelScope.launch {
            showRepository.getRankingItems().collect {
                Log.d(TAG, "getRankingItems() received ${it.size} items")
                _uiState.value = _uiState.value.copy(rankingItems = it)
            }
        }
        viewModelScope.launch {
            showRepository.getOpenSchedule().collect {
                Log.d(TAG, "getOpenSchedule() received ${it.size} items")
                _uiState.value = _uiState.value.copy(openSchedule = it)
            }
        }
        viewModelScope.launch {
            showRepository.getShows().collect {
                Log.d(TAG, "getShows() received ${it.size} shows")
                _uiState.value = _uiState.value.copy(shows = it)
            }
        }
        viewModelScope.launch {
            resaleRepository.getResaleItems().collect {
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
