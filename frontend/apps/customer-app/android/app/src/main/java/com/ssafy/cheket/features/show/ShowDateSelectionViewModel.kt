package com.ssafy.cheket.features.show

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.GradeDto
import com.ssafy.cheket.core.network.dto.SessionDto
import com.ssafy.cheket.core.network.service.ShowService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiItem(
    val sessionId: Long,
    val date: String,        // "yyyy-MM-dd"
    val startTime: String,   // "HH:mm:ss"
    val remainingSeats: Int,
    val totalSeats: Int,
) {
    /** 표시용: "2026.04.12 (토) 18:00" */
    val displayLabel: String
        get() {
            val timePart = startTime.take(5) // "18:00"
            val datePart = date.replace("-", ".") // "2026.04.12"
            val dow = try {
                val parts = date.split("-")
                val cal = java.util.Calendar.getInstance().apply {
                    set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
                val weekdays = arrayOf("일", "월", "화", "수", "목", "금", "토")
                weekdays[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            } catch (_: Exception) { "" }
            return "$datePart ($dow) $timePart"
        }

    /** 회차 넘버링용: "DAY N" 은 외부에서 계산 */
    val dateKey: String get() = date
}

data class GradeUiItem(
    val name: String,
    val price: Int,
    val colorCode: String?,
)

sealed class DateSelectionUiState {
    data object Loading : DateSelectionUiState()
    data class Error(val message: String) : DateSelectionUiState()
    data class Success(
        val showTitle: String,
        val sessions: List<SessionUiItem>,
        val grades: List<GradeUiItem>,
        /** 날짜 key("yyyy-MM-dd") → 해당 날짜의 세션 목록 */
        val sessionsByDate: Map<String, List<SessionUiItem>>,
        /** 캘린더에 표시할 연/월 */
        val calYear: Int,
        val calMonth: Int,
        /** 현재 선택된 날짜의 세션 */
        val selectedDateSessions: List<SessionUiItem> = emptyList(),
    ) : DateSelectionUiState()
}

class ShowDateSelectionViewModel(
    private val showId: String,
    private val showService: ShowService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DateSelectionUiState>(DateSelectionUiState.Loading)
    val uiState: StateFlow<DateSelectionUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DateSelectionUiState.Loading
            try {
                val showIdLong = showId.toLong()

                // 공연 상세 + 세션 목록 호출
                val showResponse = showService.getShowDetail(showIdLong)
                val sessionsResponse = showService.getSessions(showIdLong)

                val showDetail = showResponse.data
                val sessionDtos = sessionsResponse.data

                if (sessionDtos.isNullOrEmpty()) {
                    _uiState.value = DateSelectionUiState.Error("등록된 회차가 없습니다")
                    return@launch
                }

                Log.d(TAG, "load() sessions=${sessionDtos.size}, show=${showDetail?.title}")

                val sessions = sessionDtos.map { it.toUiItem() }
                val sessionsByDate = sessions.groupBy { it.dateKey }

                // 첫 세션 날짜로 캘린더 월 결정
                val firstDate = sessions.first().date
                val parts = firstDate.split("-")
                val calYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
                val calMonth = parts.getOrNull(1)?.toIntOrNull() ?: 1

                val grades = showDetail?.grade
                    ?.map {
                        GradeUiItem(
                            name = it.gradeName,
                            price = it.price,
                            colorCode = it.colorCode,
                        )
                    }
                    ?.distinctBy { Triple(it.name, it.price, it.colorCode) }
                    ?: emptyList()

                _uiState.value = DateSelectionUiState.Success(
                    showTitle = showDetail?.title ?: "공연",
                    sessions = sessions,
                    grades = grades,
                    sessionsByDate = sessionsByDate,
                    calYear = calYear,
                    calMonth = calMonth,
                )
            } catch (e: Exception) {
                Log.e(TAG, "load() error", e)
                _uiState.value = DateSelectionUiState.Error(
                    "회차 정보를 불러오지 못했습니다: ${e.message}"
                )
            }
        }
    }

    fun selectDate(dateKey: String) {
        val current = _uiState.value
        if (current is DateSelectionUiState.Success) {
            val sessionsOnDate = current.sessionsByDate[dateKey] ?: emptyList()
            _uiState.update { current.copy(selectedDateSessions = sessionsOnDate) }
        }
    }

    companion object {
        private const val TAG = "ShowDateSelectionVM"

        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                ShowDateSelectionViewModel(
                    showId = showId,
                    showService = app.appContainer.showService,
                )
            }
        }
    }
}

private fun SessionDto.toUiItem() = SessionUiItem(
    sessionId = sessionId,
    date = sessionDate,
    startTime = sessionStartTime,
    remainingSeats = remainingSeats,
    totalSeats = totalSeats,
)
