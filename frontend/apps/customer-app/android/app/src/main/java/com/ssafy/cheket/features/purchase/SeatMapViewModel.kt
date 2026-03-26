package com.ssafy.cheket.features.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.SeatMapSection
import com.ssafy.cheket.core.model.SeatPosition
import com.ssafy.cheket.core.model.SectionBounds
import com.ssafy.cheket.core.model.SectionSeat
import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.network.dto.SeatLockRequest
import com.ssafy.cheket.core.network.dto.SeatLockResponse
import com.ssafy.cheket.core.network.dto.SeatLockSeatItem
import com.ssafy.cheket.core.network.dto.SeatSectionDto
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.network.service.TicketService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeatMapUiState(
    val show: Show? = null,
    val sections: List<SeatMapSection> = emptyList(),
    val sectionBounds: Map<Long, SectionBounds> = emptyMap(),
    val seatPositions: Map<Long, SeatPosition> = emptyMap(),
    val selectedSeatIds: Set<Long> = emptySet(),   // sessionSeatId 기준
    val maxSeats: Int = 4,
    val isLoading: Boolean = true,
    val venueIndex: Int = 0,
    val isTestMode: Boolean = false,
    val errorMessage: String? = null,
    // 좌석 선점
    val isLocking: Boolean = false,
    val lockFailedSeats: List<SeatLockSeatItem> = emptyList(),
    val showLockFailedDialog: Boolean = false,
)

class SeatMapViewModel(
    private val showId: String,
    private val sessionId: String,
    private val showService: ShowService? = null,
    private val ticketService: TicketService? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatMapUiState())
    val uiState: StateFlow<SeatMapUiState> = _uiState.asStateFlow()

    /** 테스트 모드 여부: sessionId가 비어있으면 테스트 모드 */
    private val isTestMode = sessionId.isBlank()

    init {
        if (isTestMode) {
            _uiState.update { it.copy(isTestMode = true) }
            loadVenue(0)
        } else {
            loadFromApi()
        }
    }

    // ── API에서 좌석 데이터 로드 ──

    private fun loadFromApi() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val service = showService
                    ?: throw IllegalStateException("ShowService not provided")

                val showIdLong = showId.toLong()
                val sessionIdLong = sessionId.toLong()

                // 공연 정보 조회
                val showResponse = service.getShowDetail(showIdLong)
                val showDetail = showResponse.data

                // 좌석 조회
                val seatsResponse = service.getSeats(showIdLong, sessionIdLong)
                val sectionDtos = seatsResponse.data

                if (sectionDtos.isNullOrEmpty()) {
                    Log.w(TAG, "loadFromApi() empty seats response")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "좌석 정보가 없습니다",
                        )
                    }
                    return@launch
                }

                Log.d(TAG, "loadFromApi() sections=${sectionDtos.size}, totalSeats=${sectionDtos.sumOf { it.seats.size }}")

                // seat status distribution logging
                val statusCounts = sectionDtos.flatMap { it.seats }.groupingBy { it.status }.eachCount()
                Log.d(TAG, "loadFromApi() seat status distribution: $statusCounts")
                sectionDtos.flatMap { it.seats }.take(5).forEach { seat ->
                    Log.d(TAG, "  seat sample: id=${seat.sessionSeatId}, seatNo=${seat.seatNo}, status='${seat.status}', row=${seat.rowNum}, col=${seat.colNum}")
                }

                // DTO → Domain Model 변환
                val sections = sectionDtos.map { dto -> dto.toDomain() }

                // 레이아웃 계산
                val (positions, bounds) = MockDataSource.calculateLayout(sections)

                val show = showDetail?.let { d ->
                    Show(
                        id = d.showId.toString(),
                        name = d.title,
                        artistName = d.artist,
                        date = d.show?.showStartDate ?: "",
                        venue = d.venue,
                        region = d.region,
                        poster = d.posterUrl,
                        status = when (d.status) {
                            "UPCOMING" -> com.ssafy.cheket.core.model.ShowStatus.UPCOMING
                            "SOLD_OUT" -> com.ssafy.cheket.core.model.ShowStatus.SOLD_OUT
                            "COMPLETED" -> com.ssafy.cheket.core.model.ShowStatus.COMPLETED
                            else -> com.ssafy.cheket.core.model.ShowStatus.ON_SALE
                        },
                        maxPerUser = d.purchaseLimit ?: 4,
                        grades = d.grade.map { g ->
                            com.ssafy.cheket.core.model.Grade(
                                name = g.gradeName,
                                price = g.price,
                                color = g.colorCode,
                            )
                        },
                        description = d.description,
                        isLiked = d.isLiked,
                        likeCount = d.likeCount,
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        show = show,
                        sections = sections,
                        sectionBounds = bounds,
                        seatPositions = positions,
                        maxSeats = show?.maxPerUser ?: 4,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadFromApi() error", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "좌석 정보를 불러오지 못했습니다: ${e.message}",
                    )
                }
            }
        }
    }

    fun retry() {
        if (isTestMode) {
            loadVenue(_uiState.value.venueIndex)
        } else {
            loadFromApi()
        }
    }

    // ── 테스트 모드: 공연장 프리셋 로드 ──

    fun switchVenue(index: Int) {
        if (!isTestMode) return
        if (index == _uiState.value.venueIndex) return
        _uiState.update { it.copy(venueIndex = index, selectedSeatIds = emptySet()) }
        loadVenue(index)
    }

    private fun loadVenue(index: Int) {
        Log.d(TAG, "loadVenue() index=$index")
        val show = MockDataSource.mockShows.find { it.id == showId }
        val sections = MockDataSource.generateVenuePreset(index)
        val (positions, bounds) = MockDataSource.calculateLayout(sections)

        Log.d(TAG, "loadVenue() sections=${sections.size}, totalSeats=${sections.sumOf { it.seats.size }}")

        _uiState.update { state ->
            state.copy(
                show = show,
                sections = sections,
                sectionBounds = bounds,
                seatPositions = positions,
                maxSeats = show?.maxPerUser ?: 4,
                isLoading = false,
                venueIndex = index,
            )
        }
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
        val colorCode: String = "",
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
                        colorCode = section.colorCode,
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

    /** 결제 화면으로 이동 전 NavParams에 선택 정보 저장 */
    fun saveToNavParams() {
        val state = _uiState.value
        val details = getSelectedSeatDetails()
        NavParams.showInfo = state.show
        NavParams.selectedSeats = details.map { info ->
            Seat(
                id = info.sessionSeatId.toString(),
                row = info.sectionName,
                number = info.seatNo.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0,
                grade = info.gradeName,
                price = info.price,
                status = com.ssafy.cheket.core.model.SeatStatus.AVAILABLE,
            )
        }
        NavParams.totalPrice = totalPrice
        NavParams.sessionId = sessionId.toLongOrNull() ?: 0L
        Log.d(TAG, "saveToNavParams() seats=${details.size}, totalPrice=$totalPrice, sessionId=$sessionId, show=${state.show?.name}")
    }

    /**
     * 좌석 선점 API 호출 후 결과에 따라 결제 화면 이동 또는 실패 모달 표시.
     * @param onSuccess 모든 좌석 선점 성공 시 콜백 (결제 화면으로 이동)
     */
    fun lockSeatsAndProceed(onSuccess: () -> Unit) {
        val seatAccessToken = NavParams.seatAccessToken
        if (seatAccessToken.isNullOrBlank()) {
            Log.e(TAG, "lockSeatsAndProceed() — no Seat-Access-Token")
            _uiState.update { it.copy(errorMessage = "좌석 접근 토큰이 없습니다. 대기열을 통해 다시 진입해주세요.") }
            return
        }

        val selectedIds = _uiState.value.selectedSeatIds.toList()
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLocking = true) }
            try {
                val showIdLong = showId.toLong()
                val sessionIdLong = sessionId.toLong()

                Log.d(TAG, "lockSeatsAndProceed() — showId=$showIdLong, sessionId=$sessionIdLong, seats=$selectedIds")

                val response = ticketService!!.lockSeats(
                    seatAccessToken = seatAccessToken,
                    showId = showIdLong,
                    sessionId = sessionIdLong,
                    request = SeatLockRequest(sessionSeatIds = selectedIds),
                )

                val lockData = response.data
                if (lockData == null) {
                    _uiState.update { it.copy(isLocking = false, errorMessage = "좌석 선점 응답이 없습니다.") }
                    return@launch
                }

                val failedSeats = lockData.seats.failure
                if (failedSeats.isNotEmpty()) {
                    // 일부 좌석 선점 실패
                    Log.w(TAG, "lockSeatsAndProceed() — ${failedSeats.size} seats failed")
                    _uiState.update {
                        it.copy(
                            isLocking = false,
                            lockFailedSeats = failedSeats,
                            showLockFailedDialog = true,
                            // 실패한 좌석은 선택 해제
                            selectedSeatIds = it.selectedSeatIds - failedSeats.map { s -> s.sessionSeatId }.toSet(),
                        )
                    }
                    // 좌석맵 새로고침
                    reloadSeats()
                } else {
                    // 모든 좌석 선점 성공 → NavParams에 저장 후 결제로 이동
                    Log.d(TAG, "lockSeatsAndProceed() — all ${lockData.seats.success.size} seats locked, expiresAt=${lockData.expiresAt}")
                    saveToNavParams()
                    NavParams.seatAccessExpiresAt = lockData.expiresAt
                    _uiState.update { it.copy(isLocking = false) }
                    onSuccess()
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = try {
                    val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                    json.get("errorMessage")?.asString ?: json.get("responseMessage")?.asString
                } catch (_: Exception) { null }
                Log.e(TAG, "lockSeatsAndProceed() — HTTP ${e.code()}: $errorMsg", e)
                _uiState.update { it.copy(isLocking = false, errorMessage = errorMsg ?: "좌석 선점에 실패했습니다.") }
            } catch (e: Exception) {
                Log.e(TAG, "lockSeatsAndProceed() — failed", e)
                _uiState.update { it.copy(isLocking = false, errorMessage = "좌석 선점에 실패했습니다: ${e.message}") }
            }
        }
    }

    /** 실패 모달 닫기 */
    fun dismissLockFailedDialog() {
        _uiState.update { it.copy(showLockFailedDialog = false, lockFailedSeats = emptyList()) }
    }

    /** 좌석 데이터 새로고침 (선점 실패 후) */
    private fun reloadSeats() {
        if (isTestMode || showService == null) return
        viewModelScope.launch {
            try {
                val showIdLong = showId.toLong()
                val sessionIdLong = sessionId.toLong()
                val seatsResponse = showService.getSeats(showIdLong, sessionIdLong)
                val sectionDtos = seatsResponse.data ?: return@launch
                val newSections = sectionDtos.map { it.toDomain() }
                _uiState.update { state -> state.copy(sections = newSections) }
                Log.d(TAG, "reloadSeats() — refreshed ${newSections.sumOf { sec -> sec.seats.size }} seats")
            } catch (e: Exception) {
                Log.e(TAG, "reloadSeats() failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "SeatMapViewModel"

        /** 테스트 모드용 Factory (sessionId 없음) */
        fun factory(showId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SeatMapViewModel(showId = showId, sessionId = "")
            }
        }

        /** API 연결용 Factory (showId + sessionId + showService + ticketService) */
        fun factory(showId: String, sessionId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                SeatMapViewModel(
                    showId = showId,
                    sessionId = sessionId,
                    showService = app.appContainer.showService,
                    ticketService = app.appContainer.ticketService,
                )
            }
        }
    }
}

/** DTO → Domain 변환 */
private fun SeatSectionDto.toDomain(): SeatMapSection = SeatMapSection(
    sectionId = sectionId,
    sectionName = sectionName,
    gradeName = gradeName,
    price = price,
    colorCode = colorCode,
    seats = seats.map { seat ->
        SectionSeat(
            sessionSeatId = seat.sessionSeatId,
            seatId = seat.seatId,
            rowNum = seat.rowNum,
            colNum = seat.colNum,
            seatNo = seat.seatNo,
            status = seat.status,
        )
    },
)
