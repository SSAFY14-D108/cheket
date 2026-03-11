package com.ssafy.cheket.features.purchase

import android.util.Log
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
        Log.d(TAG, "init — eventId=$eventId")
        val event = MockDataSource.mockEvents.find { it.id == eventId }
        Log.d(TAG, "init — event found: ${event != null}, maxPerUser=${event?.maxPerUser}")
        _uiState.value = SeatSelectionUiState(
            event = event,
            maxSeats = event?.maxPerUser ?: 4,
        )
    }

    fun selectGrade(grade: Grade) {
        Log.d(TAG, "selectGrade() grade=${grade.name}")
        val seats = MockDataSource.generateSeats(eventId, grade.name)
        Log.d(TAG, "selectGrade() generated ${seats.size} seats")
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
        if (seat.status != SeatStatus.AVAILABLE) {
            Log.d(TAG, "toggleSeat() seat=${seat.id} not available (status=${seat.status})")
            return
        }

        _uiState.update { state ->
            val currentSelected = state.selectedSeats
            val isAlreadySelected = currentSelected.any { it.id == seat.id }
            val newSelected = if (isAlreadySelected) {
                Log.d(TAG, "toggleSeat() deselecting seat=${seat.id}")
                currentSelected.filter { it.id != seat.id }
            } else {
                if (currentSelected.size >= state.maxSeats) {
                    Log.d(TAG, "toggleSeat() max seats reached (${state.maxSeats}), ignoring seat=${seat.id}")
                    return@update state
                }
                Log.d(TAG, "toggleSeat() selecting seat=${seat.id}")
                currentSelected + seat
            }
            state.copy(selectedSeats = newSelected)
        }
    }

    fun goBackToGradeStep() {
        Log.d(TAG, "goBackToGradeStep()")
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
        Log.d(TAG, "saveToNavParams() seats=${state.selectedSeats.size}, totalPrice=${NavParams.totalPrice}")
    }

    val totalPrice: Int
        get() = _uiState.value.selectedSeats.sumOf { it.price }

    companion object {
        private const val TAG = "SeatSelectionViewModel"

        fun factory(eventId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SeatSelectionViewModel(eventId)
            }
        }
    }
}
