package com.ssafy.cheket.features.shows

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.network.dto.SaveSearchKeywordRequest
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.repository.ShowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SortOption(val label: String, val apiValue: String) {
    POPULAR("\uc778\uae30\uc21c", "POPULAR"),
    LATEST("\ucd5c\uc2e0\uc21c", "LATEST"),
    CLOSING("\ub9c8\uac10\uc784\ubc15\uc21c", "DEADLINE"),
    OPEN_SOON("\uc624\ud508\uc784\ubc15\uc21c", "OPEN_SOON"),
}

enum class RegionOption(val label: String, val apiValue: Int) {
    SEOUL("\uc11c\uc6b8", 11),
    GYEONGGI("\uacbd\uae30", 41),
    INCHEON("\uc778\ucc9c", 28),
    BUSAN("\ubd80\uc0b0", 26),
    DAEGU("\ub300\uad6c", 27),
    GWANGJU("\uad11\uc8fc", 29),
    DAEJEON("\ub300\uc804", 30),
    ULSAN("\uc6b8\uc0b0", 31),
    SEJONG("\uc138\uc885", 36),
    GANGWON("\uac15\uc6d0", 42),
    CHUNGBUK("\ucda9\ubd81", 43),
    CHUNGNAM("\ucda9\ub0a8", 44),
    JEONBUK("\uc804\ubd81", 45),
    JEONNAM("\uc804\ub0a8", 46),
    GYEONGBUK("\uacbd\ubd81", 47),
    GYEONGNAM("\uacbd\ub0a8", 48),
    JEJU("\uc81c\uc8fc", 50),
}

data class ShowsUiState(
    val shows: List<Show> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.POPULAR,
    val selectedRegions: List<RegionOption> = emptyList(),
    val includeEnded: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
)

class ShowsViewModel(
    private val showRepository: ShowRepository,
    private val showService: ShowService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowsUiState())
    val uiState: StateFlow<ShowsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var loadRequestVersion: Long = 0L

    init {
        val initialSort = NavParams.initialSortOption
        if (initialSort != null) {
            NavParams.initialSortOption = null
            val sortOption = SortOption.entries.find { it.apiValue == initialSort }
            if (sortOption != null) {
                _uiState.value = _uiState.value.copy(sortBy = sortOption)
                Log.d(TAG, "init - initial sort from NavParams: $sortOption")
            }
        }
        Log.d(TAG, "init - loading shows")
        loadShows()
    }

    fun onSearchChange(query: String) {
        Log.d(TAG, "onSearchChange() query=$query")
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadShows(page = 0)
        }
    }

    fun onSearchSubmit() {
        val query = _uiState.value.searchQuery.trim()
        Log.d(TAG, "onSearchSubmit() query=$query")
        searchJob?.cancel()
        loadShows(page = 0)

        if (query.isNotBlank()) {
            viewModelScope.launch {
                try {
                    showService.saveSearchKeyword(SaveSearchKeywordRequest(keyword = query))
                    Log.d(TAG, "saveSearchKeyword() saved: $query")
                } catch (e: Exception) {
                    Log.w(TAG, "saveSearchKeyword() failed (non-critical)", e)
                }
            }
        }
    }

    fun onSortChange(sort: SortOption) {
        Log.d(TAG, "onSortChange() sort=$sort")
        _uiState.value = _uiState.value.copy(sortBy = sort)
        loadShows(page = 0)
    }

    fun onRegionToggle(region: RegionOption?) {
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

    fun applyRegions(regions: List<RegionOption>) {
        Log.d(TAG, "applyRegions() regions=$regions")
        _uiState.value = _uiState.value.copy(selectedRegions = regions.distinct())
        loadShows(page = 0)
    }

    fun goToPage(page: Int) {
        Log.d(TAG, "goToPage() page=$page")
        if (page in 0 until _uiState.value.totalPages) {
            loadShows(page = page)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isRefreshing) return
        if (!state.hasMore) return

        loadShows(page = state.currentPage + 1, append = true)
    }

    fun refresh() {
        Log.d(TAG, "refresh()")
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadShows(page = 0)
    }

    fun onIncludeEndedChange(includeEnded: Boolean) {
        Log.d(TAG, "onIncludeEndedChange() includeEnded=$includeEnded")
        _uiState.value = _uiState.value.copy(includeEnded = includeEnded)
        loadShows(page = 0)
    }

    fun resetFilters() {
        Log.d(TAG, "resetFilters()")
        _uiState.value = _uiState.value.copy(selectedRegions = emptyList(), includeEnded = false)
        loadShows(page = 0)
    }

    fun hasActiveFilters(): Boolean = _uiState.value.selectedRegions.isNotEmpty() || _uiState.value.includeEnded
    fun activeFilterCount(): Int = _uiState.value.selectedRegions.size + if (_uiState.value.includeEnded) 1 else 0

    private fun loadShows(page: Int = 0, append: Boolean = false) {
        loadJob?.cancel()
        val requestVersion = ++loadRequestVersion
        loadJob = viewModelScope.launch {
            val s = _uiState.value
            if (!s.isRefreshing) {
                _uiState.value = s.copy(
                    isLoading = !append,
                    isLoadingMore = append,
                )
            }

            val keyword = s.searchQuery.trim().ifBlank { null }
            val regions = s.selectedRegions.map { it.apiValue }.ifEmpty { null }
            val includeEnded = if (s.includeEnded) true else null

            try {
                val result = showRepository.getShowsPage(
                    regions = regions,
                    sort = s.sortBy.apiValue,
                    keyword = keyword,
                    includeEnded = includeEnded,
                    page = page,
                    size = PAGE_SIZE,
                )

                if (requestVersion != loadRequestVersion) {
                    Log.d(TAG, "loadShows() ignoring stale response version=$requestVersion latest=$loadRequestVersion")
                    return@launch
                }

                val mergedShows = if (append) {
                    val existingIds = s.shows.map { it.id }.toSet()
                    s.shows + result.shows.filter { it.id !in existingIds }
                } else {
                    result.shows
                }
                val hasMore = when {
                    result.totalElements > 0 -> mergedShows.size < result.totalElements
                    else -> result.shows.size >= PAGE_SIZE
                }

                _uiState.value = _uiState.value.copy(
                    shows = mergedShows,
                    currentPage = result.page,
                    totalPages = result.totalPages,
                    totalElements = result.totalElements,
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                    hasMore = hasMore,
                )
                Log.d(
                    TAG,
                    "loadShows() page=${result.page}/${result.totalPages}, total=${result.totalElements}, count=${result.shows.size}",
                )
            } catch (_: CancellationException) {
                Log.d(TAG, "loadShows() cancelled version=$requestVersion")
            } catch (e: Exception) {
                Log.e(TAG, "loadShows() failed", e)
                if (requestVersion != loadRequestVersion) return@launch
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                )
            }
        }
    }

    companion object {
        private const val TAG = "ShowsViewModel"
        private const val PAGE_SIZE = 10

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ShowsViewModel(app.appContainer.showRepository, app.appContainer.showService)
            }
        }
    }
}
