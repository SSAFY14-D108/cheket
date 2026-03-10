package com.ssafy.cheket.features.concerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOption(val label: String) { POPULAR("인기순"), LATEST("최신순"), CLOSING("오픈임박순") }

data class ConcertsUiState(
    val allEvents: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.POPULAR,
    val selectedRegions: List<String> = emptyList(),
    val isFilterExpanded: Boolean = false,
    val isLoading: Boolean = true,
)

class ConcertsViewModel(private val eventRepository: EventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ConcertsUiState())
    val uiState: StateFlow<ConcertsUiState> = _uiState.asStateFlow()
    val regions = listOf("서울", "경기", "인천", "부산", "대구", "대전", "광주", "제주")
    private val popularityOrder = listOf("evt_001", "evt_002", "evt_004", "evt_006", "evt_007", "evt_008")

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

    fun onRegionToggle(region: String?) {
        if (region == null) {
            _uiState.value = _uiState.value.copy(selectedRegions = emptyList())
        } else {
            val current = _uiState.value.selectedRegions
            val updated = if (current.contains(region)) current - region else current + region
            _uiState.value = _uiState.value.copy(selectedRegions = updated)
        }
        applyFilters()
    }

    fun toggleFilter() { _uiState.value = _uiState.value.copy(isFilterExpanded = !_uiState.value.isFilterExpanded) }
    fun resetFilters() { _uiState.value = _uiState.value.copy(selectedRegions = emptyList()); applyFilters() }
    fun hasActiveFilters(): Boolean = _uiState.value.selectedRegions.isNotEmpty()
    fun activeFilterCount(): Int = _uiState.value.selectedRegions.size

    private fun applyFilters() {
        val s = _uiState.value
        var result = s.allEvents

        if (s.searchQuery.isNotBlank()) {
            val keyword = s.searchQuery.trim().lowercase()
            result = result.filter {
                it.name.lowercase().contains(keyword) ||
                    it.venue.lowercase().contains(keyword) ||
                    (it.artistName ?: "").lowercase().contains(keyword)
            }
        }

        if (s.selectedRegions.isNotEmpty()) {
            result = result.filter { it.region in s.selectedRegions }
        }

        result = when (s.sortBy) {
            SortOption.POPULAR -> result.sortedBy { e ->
                val idx = popularityOrder.indexOf(e.id)
                if (idx == -1) 99 else idx
            }
            SortOption.LATEST -> result.sortedByDescending { it.openDate ?: "" }
            SortOption.CLOSING -> result.sortedBy { it.openDate ?: "9999" }
        }

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
