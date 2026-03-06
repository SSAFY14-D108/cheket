package com.ssafy.cheket.features.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
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
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.EventDate
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import java.util.Calendar

private val KR_WEEKDAYS = listOf("일", "월", "화", "수", "목", "금", "토")

private fun parseDateFromLabel(label: String): Triple<Int, Int, Int>? {
    val match = Regex("""(\d{4})\.(\d{2})\.(\d{2})""").find(label) ?: return null
    return Triple(
        match.groupValues[1].toInt(),
        match.groupValues[2].toInt(),
        match.groupValues[3].toInt(),
    )
}

@Composable
fun EventDateSelectionScreen(
    eventId: String,
    onDateSelected: (eventId: String, eventDateId: String) -> Unit,
    onBack: () -> Unit,
) {
    val event = remember { MockDataSource.mockEvents.find { it.id == eventId } }
    var selectedDayShows by remember { mutableStateOf<List<EventDate>>(emptyList()) }

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("공연을 찾을 수 없습니다.", color = MutedForeground, fontSize = 16.sp)
        }
        return
    }

    val eventDates = remember(event) {
        if (event.dates.isNotEmpty()) event.dates
        else listOf(EventDate(id = "${event.id}_d1", label = event.date, day = "DAY 1"))
    }

    val firstParsed = remember(eventDates) {
        parseDateFromLabel(eventDates.first().label) ?: Triple(2026, 1, 1)
    }
    val calYear = firstParsed.first
    val calMonth = firstParsed.second

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

    val availableMap = remember(eventDates) {
        val map = mutableMapOf<String, MutableList<EventDate>>()
        eventDates.forEach { d ->
            val parsed = parseDateFromLabel(d.label) ?: return@forEach
            val key = "${parsed.first}-${parsed.second.toString().padStart(2, '0')}-${parsed.third.toString().padStart(2, '0')}"
            map.getOrPut(key) { mutableListOf() }.add(d)
        }
        map
    }

    Scaffold(
        topBar = { AppHeader(title = "회차 선택", onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "날짜/회차를 먼저 선택하면 대기열에 입장합니다.",
                fontSize = 12.sp,
                color = MutedForeground,
            )

            // Calendar Card
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
                            contentDescription = null,
                            tint = MutedForeground.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            "${calYear}.${calMonth.toString().padStart(2, '0')}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MutedForeground.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp),
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
                                            val key = "${calYear}-${calMonth.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                                            val datesOnDay = availableMap[key] ?: emptyList()
                                            val hasShow = datesOnDay.isNotEmpty()
                                            val dow = (firstDayOfWeek + day - 1) % 7

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .then(
                                                        if (hasShow) Modifier.clickable {
                                                            selectedDayShows = datesOnDay
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
                                                            if (hasShow) Modifier.background(Primary)
                                                            else Modifier
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        "$day",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (hasShow) White
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
                                                            .background(Primary),
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

            // Selected day's shows
            if (selectedDayShows.isNotEmpty()) {
                Text(
                    "선택 가능한 회차",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                )
                selectedDayShows.forEach { d ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateSelected(event.id, d.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        d.day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        d.label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnBackground,
                                    )
                                }
                                Icon(
                                    Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = MutedForeground,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            // Grade pills
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                event.grades.forEach { g ->
                                    val gradeColor = g.color?.let {
                                        try { Color(android.graphics.Color.parseColor(it)) }
                                        catch (_: Exception) { MutedForeground }
                                    } ?: MutedForeground

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(gradeColor.copy(alpha = 0.1f))
                                            .border(1.dp, gradeColor.copy(alpha = 0.3f), RoundedCornerShape(50))
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
                                            if (g.remaining == 0) "매진"
                                            else "${g.remaining}석",
                                            fontSize = 11.sp,
                                            fontWeight = if (g.remaining == 0) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (g.remaining == 0) Danger else gradeColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
