package com.ssafy.cheket.features.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.SeatMapSection
import com.ssafy.cheket.core.model.SectionBounds
import com.ssafy.cheket.core.model.SectionSeat
import com.ssafy.cheket.core.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SeatMapUiState(
    val show: Show? = null,
    val sections: List<SeatMapSection> = emptyList(),
    val sectionBounds: Map<Long, SectionBounds> = emptyMap(),
    val selectedSeatIds: Set<Long> = emptySet(),   // sessionSeatId 기준
    val maxSeats: Int = 4,
    val isLoading: Boolean = true,
)

class SeatMapViewModel(
    private val showId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatMapUiState())
    val uiState: StateFlow<SeatMapUiState> = _uiState.asStateFlow()

    init {
        loadSeatMap()
    }

    private fun loadSeatMap() {
        Log.d(TAG, "loadSeatMap() showId=$showId")
        val show = MockDataSource.mockShows.find { it.id == showId }
        val sections = MockDataSource.generateSeatMapSections(showId)
        val bounds = MockDataSource.calculateSectionBounds(sections)

        Log.d(TAG, "loadSeatMap() sections=${sections.size}, totalSeats=${sections.sumOf { it.seats.size }}")

        _uiState.value = SeatMapUiState(
            show = show,
            sections = sections,
            sectionBounds = bounds,
            maxSeats = show?.maxPerUser ?: 4,
            isLoading = false,
        )
    }

    fun toggleSeat(seat: SectionSeat) {
        if (seat.status != "AVAILABLE") {
            Log.d(TAG, "toggleSeat() seat=${seat.sessionSeatId} not available (status=${seat.status})")
            return
        }

        _uiState.update { state ->
            val isSelected = seat.sessionSeatId in state.selectedSeatIds
            if (isSelected) {
                Log.d(TAG, "toggleSeat() deselecting seat=${seat.sessionSeatId}")
                state.copy(selectedSeatIds = state.selectedSeatIds - seat.sessionSeatId)
            } else {
                if (state.selectedSeatIds.size >= state.maxSeats) {
                    Log.d(TAG, "toggleSeat() max seats reached (${state.maxSeats})")
                    return@update state
                }
                Log.d(TAG, "toggleSeat() selecting seat=${seat.sessionSeatId}")
                state.copy(selectedSeatIds = state.selectedSeatIds + seat.sessionSeatId)
            }
        }
    }

    fun isSeatSelected(sessionSeatId: Long): Boolean =
        sessionSeatId in _uiState.value.selectedSeatIds

    /** 선택된 좌석들의 상세 정보 (하단 패널 표시용) */
    data class SelectedSeatInfo(
        val sessionSeatId: Long,
        val seatNo: String,
        val sectionName: String,
        val gradeName: String,
        val price: Int,
    )

    fun getSelectedSeatDetails(): List<SelectedSeatInfo> {
        val state = _uiState.value
        return state.sections.flatMap { section ->
            section.seats
                .filter { it.sessionSeatId in state.selectedSeatIds }
                .map { seat ->
                    SelectedSeatInfo(
                        sessionSeatId = seat.sessionSeatId,
                        seatNo = seat.seatNo,
                        sectionName = section.sectionName,
                        gradeName = section.gradeName,
                        price = section.price,
                    )
                }
        }
    }

    val totalPrice: Int
        get() {
            val state = _uiState.value
            return state.sections.sumOf { section ->
                section.seats.count { it.sessionSeatId in state.selectedSeatIds } * section.price
            }
        }

    companion object {
        private const val TAG = "SeatMapViewModel"

        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SeatMapViewModel(showId)
            }
        }
    }
}
