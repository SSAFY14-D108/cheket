package com.ssafy.cheket.features.show

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

// v0 design tokens
private val V0Bg = Color(0xFFF8F8F8)
private val V0TextPrimary = Color(0xFF111111)
private val V0TextMuted = Color(0xFF6B7280)
private val V0DayLabel = Color(0xFF6B7280)  // v0: text-[#6b7280] (NOT primary green)

private val KR_WEEKDAYS = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
fun ShowDateSelectionScreen(
    showId: String,
    onDateSelected: (showId: String, sessionId: String, showName: String, showDateLabel: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowDateSelectionViewModel = viewModel(
        factory = ShowDateSelectionViewModel.factory(showId)
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppHeader(title = "공연 날짜 선택", onBack = onBack) },
    ) { innerPadding ->
        when (val state = uiState) {
            is DateSelectionUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(V0Bg)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = V0TextMuted)
                }
            }

            is DateSelectionUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(V0Bg)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = V0TextMuted, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .gradientBorder(RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            TextButton(onClick = { viewModel.load() }) {
                                Text("다시 시도", color = V0TextPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            is DateSelectionUiState.Success -> {
                DateSelectionContent(
                    state = state,
                    onSelectDate = { viewModel.selectDate(it) },
                    onPrevMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                    onSessionClick = { session ->
                        onDateSelected(
                            showId,
                            session.sessionId.toString(),
                            state.showTitle,
                            session.displayLabel,
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(V0Bg)
                        .padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun DateSelectionContent(
    state: DateSelectionUiState.Success,
    onSelectDate: (dateKey: String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSessionClick: (SessionUiItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "원하는 날짜와 회차를 선택한 뒤 좌석을 고를 수 있어요.",
            fontSize = 12.sp,
            color = MutedForeground,
        )

        // ── Calendar Card ──
        CalendarCard(
            calYear = state.calYear,
            calMonth = state.calMonth,
            sessionsByDate = state.sessionsByDate,
            onDayClick = onSelectDate,
            onPrevMonth = onPrevMonth,
            onNextMonth = onNextMonth,
        )

        // ── Selected day sessions ──
        if (state.selectedDateSessions.isNotEmpty()) {
            Text(
                "선택 가능한 회차",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0TextPrimary,
            )

            // DAY 넘버링: 전체 고유 날짜 순서 기준
            val allDates = state.sessionsByDate.keys.sorted()

            state.selectedDateSessions.forEach { session ->
                val dayNumber = allDates.indexOf(session.dateKey) + 1
                val isPastSession = try {
                    LocalDateTime.of(
                        LocalDate.parse(session.date),
                        LocalTime.parse(session.startTime),
                    ) < LocalDateTime.now()
                } catch (_: Exception) { false }

                SessionCard(
                    session = session,
                    dayLabel = "DAY $dayNumber",
                    grades = state.grades,
                    numberFormat = numberFormat,
                    isPast = isPastSession,
                    onClick = { onSessionClick(session) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ══════════════════════════════════════════
//   Calendar Card
// ══════════════════════════════════════════

@Composable
private fun CalendarCard(
    calYear: Int,
    calMonth: Int,
    sessionsByDate: Map<String, List<SessionUiItem>>,
    onDayClick: (dateKey: String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val calendar = remember(calYear, calMonth) {
        Calendar.getInstance().apply { set(calYear, calMonth - 1, 1) }
    }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = remember(firstDayOfWeek, daysInMonth) {
        val list = mutableListOf<Int?>()
        repeat(firstDayOfWeek) { list.add(null) }
        for (d in 1..daysInMonth) list.add(d)
        while (list.size % 7 != 0) list.add(null)
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column {
            // Month header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "이전 달",
                    tint = MutedForeground,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onPrevMonth() }
                        .padding(4.dp),
                )
                Text(
                    "${calYear}.${calMonth.toString().padStart(2, '0')}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "다음 달",
                    tint = MutedForeground,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onNextMonth() }
                        .padding(4.dp),
                )
            }

            HorizontalDivider(color = BorderColor)

            // Weekday headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                KR_WEEKDAYS.forEachIndexed { i, wd ->
                    Text(
                        text = wd,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (i) {
                            0 -> Color(0xFFEF5350)
                            6 -> Color(0xFF42A5F5)
                            else -> MutedForeground
                        },
                    )
                }
            }

            // Day grid
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                cells.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (day != null) {
                                    val key = "${calYear}-${
                                        calMonth.toString().padStart(2, '0')
                                    }-${day.toString().padStart(2, '0')}"
                                    val sessionsOnDay = sessionsByDate[key]
                                    val isPast = try {
                                        LocalDate.parse(key) < LocalDate.now()
                                    } catch (_: Exception) { false }
                                    val hasShow = !sessionsOnDay.isNullOrEmpty() && !isPast
                                    val dow = (firstDayOfWeek + day - 1) % 7

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .then(
                                                if (hasShow) Modifier.clickable {
                                                    onDayClick(key)
                                                } else Modifier
                                            ),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (hasShow) Modifier.background(
                                                        PrimaryLight
                                                    )
                                                    else Modifier
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                "$day",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (hasShow) OnBackground
                                                else when (dow) {
                                                    0 -> Color(0xFFEF5350).copy(alpha = 0.3f)
                                                    6 -> Color(0xFF42A5F5).copy(alpha = 0.3f)
                                                    else -> MutedForeground.copy(alpha = 0.3f)
                                                },
                                            )
                                        }
                                        if (hasShow) {
                                            Box(
                                                Modifier
                                                    .padding(top = 2.dp)
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF9AA4B2)),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ══════════════════════════════════════════
//   Session Card (회차 선택 카드)
// ══════════════════════════════════════════

@Composable
private fun SessionCard(
    session: SessionUiItem,
    dayLabel: String,
    grades: List<GradeUiItem>,
    numberFormat: NumberFormat,
    isPast: Boolean = false,
    onClick: () -> Unit,
) {
    val isSoldOut = session.remainingSeats == 0
    val isDisabled = isSoldOut || isPast

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isDisabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDisabled) Color(0xFFF5F5F5) else CardBg,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .then(if (isDisabled) Modifier.fillMaxWidth() else Modifier),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 회차 + 날짜/시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        dayLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDisabled) MutedForeground else V0DayLabel,
                        letterSpacing = 0.5.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        session.displayLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDisabled) MutedForeground else V0TextPrimary,
                    )
                }
                if (isPast) {
                    Text(
                        "지난 공연",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedForeground,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE5E7EB))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                } else if (isSoldOut) {
                    Text(
                        "매진",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Danger)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                } else {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // 잔여석 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Outlined.EventSeat,
                    contentDescription = null,
                    tint = if (session.remainingSeats > 0) V0DayLabel else Danger,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    if (session.remainingSeats > 0)
                        "잔여 ${session.remainingSeats}석 / ${session.totalSeats}석"
                    else "매진",
                    fontSize = 12.sp,
                    fontWeight = if (session.remainingSeats == 0) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (session.remainingSeats > 0) V0TextMuted else Danger,
                )
            }

            // Grade pills
            if (grades.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    grades.forEach { g ->
                        val gradeColor = g.colorCode?.let {
                            try {
                                Color(android.graphics.Color.parseColor(it))
                            } catch (_: Exception) {
                                MutedForeground
                            }
                        } ?: MutedForeground

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(gradeColor.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    gradeColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                g.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = gradeColor,
                            )
                            Text(
                                "${numberFormat.format(g.price)} SSF",
                                fontSize = 11.sp,
                                color = gradeColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
