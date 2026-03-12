package com.ssafy.cheket.features.shows

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

enum class SortOption(val label: String, val apiValue: String) {
    POPULAR("인기순", "POPULAR"),
    LATEST("최신순", "LATEST"),
    CLOSING("오픈임박순", "DEADLINE"),
}

data class ShowsUiState(
    val shows: List<Show> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.POPULAR,
    val selectedRegions: List<String> = emptyList(),
    val isFilterExpanded: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
)

class ShowsViewModel(private val showRepository: ShowRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowsUiState())
    val uiState: StateFlow<ShowsUiState> = _uiState.asStateFlow()

    val regions = listOf("서울", "경기", "인천", "부산", "대구", "대전", "광주", "제주")

    // 검색 디바운싱용
    private var searchJob: Job? = null

    init {
        Log.d(TAG, "init — loading shows")
        loadShows()
    }

    fun onSearchChange(query: String) {
        Log.d(TAG, "onSearchChange() query=$query")
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // 디바운싱: 타이핑 멈춘 후 400ms 뒤에 API 호출
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadShows(page = 0)
        }
    }

    fun onSortChange(sort: SortOption) {
        Log.d(TAG, "onSortChange() sort=$sort")
        _uiState.value = _uiState.value.copy(sortBy = sort)
        loadShows(page = 0)
    }

    fun onRegionToggle(region: String?) {
        Log.d(TAG, "onRegionToggle() region=$region")
        if (region == null) {
            _uiState.value = _uiState.value.copy(selectedRegions = emptyList())
        } else {
            val current = _uiState.value.selectedRegions
            val updated = if (current.contains(region)) current - region else current + region
            _uiState.value = _uiState.value.copy(selectedRegions = updated)
        }
        loadShows(page = 0)
    }

    fun goToPage(page: Int) {
        Log.d(TAG, "goToPage() page=$page")
        if (page in 0 until _uiState.value.totalPages) {
            loadShows(page = page)
        }
    }

    fun refresh() {
        Log.d(TAG, "refresh()")
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadShows(page = _uiState.value.currentPage)
    }

    fun toggleFilter() {
        _uiState.value = _uiState.value.copy(isFilterExpanded = !_uiState.value.isFilterExpanded)
    }

    fun resetFilters() {
        Log.d(TAG, "resetFilters()")
        _uiState.value = _uiState.value.copy(selectedRegions = emptyList())
        loadShows(page = 0)
    }

    fun hasActiveFilters(): Boolean = _uiState.value.selectedRegions.isNotEmpty()
    fun activeFilterCount(): Int = _uiState.value.selectedRegions.size

    private fun loadShows(page: Int = 0) {
        viewModelScope.launch {
            val s = _uiState.value
            if (!s.isRefreshing) {
                _uiState.value = s.copy(isLoading = true)
            }

            val keyword = s.searchQuery.trim().ifBlank { null }
            // 지역: 복수 선택 시 첫 번째만 전달 (API가 단일 값)
            val region = s.selectedRegions.firstOrNull()

            val result = showRepository.getShowsPage(
                region = region,
                sort = s.sortBy.apiValue,
                keyword = keyword,
                page = page,
                size = PAGE_SIZE,
            )

            _uiState.value = _uiState.value.copy(
                shows = result.shows,
                currentPage = result.page,
                totalPages = result.totalPages,
                totalElements = result.totalElements,
                isLoading = false,
                isRefreshing = false,
            )
            Log.d(TAG, "loadShows() page=${result.page}/${result.totalPages}, total=${result.totalElements}, count=${result.shows.size}")
        }
    }

    companion object {
        private const val TAG = "ShowsViewModel"
        private const val PAGE_SIZE = 20

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ShowsViewModel(app.appContainer.showRepository)
            }
        }
    }
}
