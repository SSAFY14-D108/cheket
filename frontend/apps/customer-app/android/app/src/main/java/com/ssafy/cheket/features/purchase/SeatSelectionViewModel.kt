package com.ssafy.cheket.features.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.model.Grade
import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.SeatStatus
import com.ssafy.cheket.core.navigation.NavParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SeatSelectionStep { GRADE, SEATS }

data class SeatSelectionUiState(
    val event: Event? = null,
    val step: SeatSelectionStep = SeatSelectionStep.GRADE,
    val selectedGrade: Grade? = null,
    val seats: List<Seat> = emptyList(),
    val selectedSeats: List<Seat> = emptyList(),
    val maxSeats: Int = 4,
)

class SeatSelectionViewModel(
    private val eventId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatSelectionUiState())
    val uiState: StateFlow<SeatSelectionUiState> = _uiState.asStateFlow()

    init {
        val event = MockDataSource.mockEvents.find { it.id == eventId }
        _uiState.value = SeatSelectionUiState(
            event = event,
            maxSeats = event?.maxPerUser ?: 4,
        )
    }

    fun selectGrade(grade: Grade) {
        val seats = MockDataSource.generateSeats(eventId, grade.name)
        _uiState.update {
            it.copy(
                selectedGrade = grade,
                seats = seats,
                selectedSeats = emptyList(),
                step = SeatSelectionStep.SEATS,
            )
        }
    }

    fun toggleSeat(seat: Seat) {
        if (seat.status != SeatStatus.AVAILABLE) return

        _uiState.update { state ->
            val currentSelected = state.selectedSeats
            val isAlreadySelected = currentSelected.any { it.id == seat.id }
            val newSelected = if (isAlreadySelected) {
                currentSelected.filter { it.id != seat.id }
            } else {
                if (currentSelected.size >= state.maxSeats) {
                    // Already at max, don't add
                    return@update state
                }
                currentSelected + seat
            }
            state.copy(selectedSeats = newSelected)
        }
    }

    fun goBackToGradeStep() {
        _uiState.update {
            it.copy(
                step = SeatSelectionStep.GRADE,
                selectedGrade = null,
                seats = emptyList(),
                selectedSeats = emptyList(),
            )
        }
    }

    fun saveToNavParams() {
        val state = _uiState.value
        NavParams.selectedSeats = state.selectedSeats
        NavParams.totalPrice = state.selectedSeats.sumOf { it.price }
    }

    val totalPrice: Int
        get() = _uiState.value.selectedSeats.sumOf { it.price }

    companion object {
        fun factory(eventId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SeatSelectionViewModel(eventId)
            }
        }
    }
}
