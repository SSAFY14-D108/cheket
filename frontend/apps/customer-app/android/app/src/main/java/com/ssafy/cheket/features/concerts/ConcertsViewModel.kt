package com.ssafy.cheket.features.concerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.model.EventStatus
import com.ssafy.cheket.core.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOption(val label: String) { POPULAR("인기순"), LATEST("최신순"), CLOSING("마감임박순") }

data class ConcertsUiState(
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.POPULAR,
    val selectedRegion: String? = null,
    val showSoldOut: Boolean = true,
    val isFilterExpanded: Boolean = false,
    val isLoading: Boolean = true,
)

class ConcertsViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ConcertsUiState())
    val uiState: StateFlow<ConcertsUiState> = _uiState.asStateFlow()
    val regions = listOf("서울", "경기", "인천", "부산", "대구", "광주", "대전", "강원", "제주")

    init {
        viewModelScope.launch {
            eventRepository.getEvents().collect { events ->
                _uiState.value = _uiState.value.copy(allEvents = events, isLoading = false)
                applyFilters()
            }
        }
    }

    fun onSearchChange(query: String) { _uiState.value = _uiState.value.copy(searchQuery = query); applyFilters() }
    fun onSortChange(sort: SortOption) { _uiState.value = _uiState.value.copy(sortBy = sort); applyFilters() }
    fun onRegionSelect(region: String?) { _uiState.value = _uiState.value.copy(selectedRegion = region); applyFilters() }
    fun onSoldOutToggle() { _uiState.value = _uiState.value.copy(showSoldOut = !_uiState.value.showSoldOut); applyFilters() }
    fun toggleFilter() { _uiState.value = _uiState.value.copy(isFilterExpanded = !_uiState.value.isFilterExpanded) }
    fun resetFilters() { _uiState.value = _uiState.value.copy(selectedRegion = null, showSoldOut = true); applyFilters() }
    fun hasActiveFilters(): Boolean { val s = _uiState.value; return s.selectedRegion != null || !s.showSoldOut }
    fun activeFilterCount(): Int { var count = 0; val s = _uiState.value; if (s.selectedRegion != null) count++; if (!s.showSoldOut) count++; return count }

    private fun applyFilters() {
        val s = _uiState.value
        var result = s.allEvents
        if (s.searchQuery.isNotBlank()) result = result.filter { it.name.contains(s.searchQuery, true) || it.venue.contains(s.searchQuery, true) }
        if (s.selectedRegion != null) result = result.filter { it.region == s.selectedRegion }
        if (!s.showSoldOut) result = result.filter { it.status != EventStatus.SOLD_OUT }
        _uiState.value = s.copy(filteredEvents = result)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ConcertsViewModel(app.appContainer.eventRepository)
            }
        }
    }
}
