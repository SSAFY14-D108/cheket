package com.ssafy.cheket.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.repository.EventRepository
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
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val resaleRepository: ResaleRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            eventRepository.getBannerSlides().collect { _uiState.value = _uiState.value.copy(bannerSlides = it) }
        }
        viewModelScope.launch {
            eventRepository.getCategories().collect { _uiState.value = _uiState.value.copy(categories = it) }
        }
        viewModelScope.launch {
            eventRepository.getRankingItems().collect { _uiState.value = _uiState.value.copy(rankingItems = it) }
        }
        viewModelScope.launch {
            eventRepository.getOpenSchedule().collect { _uiState.value = _uiState.value.copy(openSchedule = it) }
        }
        viewModelScope.launch {
            eventRepository.getEvents().collect { _uiState.value = _uiState.value.copy(events = it) }
        }
        viewModelScope.launch {
            resaleRepository.getResaleItems().collect { _uiState.value = _uiState.value.copy(resaleItems = it, isLoading = false) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                HomeViewModel(app.appContainer.eventRepository, app.appContainer.resaleRepository)
            }
        }
    }
}
