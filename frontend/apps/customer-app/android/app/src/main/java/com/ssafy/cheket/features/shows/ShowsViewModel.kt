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

/** 백엔드 Region 값을 지역 필터에 매핑 */
enum class RegionOption(val label: String, val apiValue: Int) {
    SEOUL("서울", 11),
    GYEONGGI("경기", 41),
    INCHEON("인천", 28),
    BUSAN("부산", 26),
    DAEGU("대구", 27),
    GWANGJU("광주", 29),
    DAEJEON("대전", 30),
    ULSAN("울산", 31),
    SEJONG("세종", 36),
    GANGWON("강원", 42),
    CHUNGBUK("충북", 43),
    CHUNGNAM("충남", 44),
    JEONBUK("전북", 45),
    JEONNAM("전남", 46),
    GYEONGBUK("경북", 47),
    GYEONGNAM("경남", 48),
    JEJU("제주", 50),
}

data class ShowsUiState(
    val shows: List<Show> = emptyList(),
    val searchQuery: String = "",
    val sortBy: SortOption = SortOption.POPULAR,
    val selectedRegions: List<RegionOption> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Int = 0,
)

class ShowsViewModel(private val showRepository: ShowRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ShowsUiState())
    val uiState: StateFlow<ShowsUiState> = _uiState.asStateFlow()

    // 寃???붾컮?댁떛??
    private var searchJob: Job? = null

    init {
        Log.d(TAG, "init ??loading shows")
        loadShows()
    }

    // ?? 寃?? ?띿뒪???낅뜲?댄듃 + ?붾컮?댁떛 API ?몄텧 ??
    fun onSearchChange(query: String) {
        Log.d(TAG, "onSearchChange() query=$query")
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            loadShows(page = 0)
        }
    }

    // ?? 寃?? ?ㅻ낫??Search 踰꾪듉 / 寃???꾩씠肄??대┃ ??利됱떆 API ?몄텧 ??
    fun onSearchSubmit() {
        Log.d(TAG, "onSearchSubmit() query=${_uiState.value.searchQuery}")
        searchJob?.cancel()
        loadShows(page = 0)
    }

    // ?? ?뺣젹 蹂寃???利됱떆 API ?몄텧 ??
    fun onSortChange(sort: SortOption) {
        Log.d(TAG, "onSortChange() sort=$sort")
        _uiState.value = _uiState.value.copy(sortBy = sort)
        loadShows(page = 0)
    }

    // ?? 吏???꾪꽣 ?좉? ??利됱떆 API ?몄텧 ??
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

    fun resetFilters() {
        Log.d(TAG, "resetFilters()")
        _uiState.value = _uiState.value.copy(selectedRegions = emptyList())
        loadShows(page = 0)
    }

    fun hasActiveFilters(): Boolean = _uiState.value.selectedRegions.isNotEmpty()
    fun activeFilterCount(): Int = _uiState.value.selectedRegions.size

    private fun loadShows(page: Int = 0, append: Boolean = false) {
        viewModelScope.launch {
            val s = _uiState.value
            if (!s.isRefreshing) {
                _uiState.value = s.copy(
                    isLoading = !append,
                    isLoadingMore = append,
                )
            }

            val keyword = s.searchQuery.trim().ifBlank { null }
            val regions = s.selectedRegions.map { it.apiValue }.ifEmpty { null }

            val result = showRepository.getShowsPage(
                regions = regions,
                sort = s.sortBy.apiValue,
                keyword = keyword,
                page = page,
                size = PAGE_SIZE,
            )

            val mergedShows = if (append) s.shows + result.shows else result.shows
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
            Log.d(TAG, "loadShows() page=${result.page}/${result.totalPages}, total=${result.totalElements}, count=${result.shows.size}")
        }
    }

    companion object {
        private const val TAG = "ShowsViewModel"
        private const val PAGE_SIZE = 10

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ShowsViewModel(app.appContainer.showRepository)
            }
        }
    }
}
