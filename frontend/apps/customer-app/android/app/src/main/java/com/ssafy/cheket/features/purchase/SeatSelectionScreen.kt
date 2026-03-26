package com.ssafy.cheket.features.purchase

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.model.Grade
import com.ssafy.cheket.core.model.Seat
import com.ssafy.cheket.core.model.SeatStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SeatSelectionScreen(
    showId: String,
    onNavigateToPayment: (showId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: SeatSelectionViewModel = viewModel(factory = SeatSelectionViewModel.factory(showId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Scaffold(
        topBar = {
            AppHeader(
                title = when (uiState.step) {
                    SeatSelectionStep.GRADE -> "등급 선택"
                    SeatSelectionStep.SEATS -> "${uiState.selectedGrade?.name ?: ""} 좌석 선택"
                },
                onBack = {
                    when (uiState.step) {
                        SeatSelectionStep.GRADE -> onBack()
                        SeatSelectionStep.SEATS -> viewModel.goBackToGradeStep()
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    if (targetState == SeatSelectionStep.SEATS) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition",
            ) { step ->
                when (step) {
                    SeatSelectionStep.GRADE -> GradeSelectionContent(
                        grades = uiState.show?.grades ?: emptyList(),
                        numberFormat = numberFormat,
                        onSelectGrade = viewModel::selectGrade,
                    )
                    SeatSelectionStep.SEATS -> SeatSelectionContent(
                        seats = uiState.seats,
                        selectedSeats = uiState.selectedSeats,
                        maxSeats = uiState.maxSeats,
                        numberFormat = numberFormat,
                        onToggleSeat = viewModel::toggleSeat,
                        onNavigateToPayment = {
                            viewModel.saveToNavParams()
                            onNavigateToPayment(showId)
                        },
                    )
                }
            }
        }
    }
}

// --- Grade Selection Step ---

@Composable
private fun GradeSelectionContent(
    grades: List<Grade>,
    numberFormat: NumberFormat,
    onSelectGrade: (Grade) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "등급을 선택해주세요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = "원하는 좌석 등급을 선택하면 좌석 배치도가 표시됩니다.",
                fontSize = 13.sp,
                color = MutedForeground,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(grades) { grade ->
            GradeCard(
                grade = grade,
                numberFormat = numberFormat,
                onClick = { if (grade.remaining == null || grade.remaining > 0) onSelectGrade(grade) },
            )
        }
    }
}

@Composable
private fun GradeCard(
    grade: Grade,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
) {
    val isSoldOut = grade.remaining != null && grade.remaining == 0
    val gradeColor = gradeToColor(grade.name)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isSoldOut) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = if (isSoldOut) Muted else CardBg,
        shadowElevation = if (isSoldOut) 0.dp else 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Grade color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSoldOut) MutedForeground.copy(alpha = 0.2f) else gradeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.EventSeat,
                    contentDescription = null,
                    tint = if (isSoldOut) MutedForeground else gradeColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = grade.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSoldOut) MutedForeground else OnBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        isSoldOut -> "매진"
                        grade.remaining != null -> "잔여 ${grade.remaining}석"
                        else -> "선택 가능"
                    },
                    fontSize = 12.sp,
                    color = when {
                        isSoldOut -> Danger
                        else -> MutedForeground
                    },
                    fontWeight = FontWeight.Medium,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "${numberFormat.format(grade.price)} SSF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSoldOut) MutedForeground else OnBackground,
                )
                if (isSoldOut) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SOLD OUT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Danger)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

// --- Seat Selection Step ---

@Composable
private fun SeatSelectionContent(
    seats: List<Seat>,
    selectedSeats: List<Seat>,
    maxSeats: Int,
    numberFormat: NumberFormat,
    onToggleSeat: (Seat) -> Unit,
    onNavigateToPayment: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SeatLegendItem(color = Primary, label = "선택")
            SeatLegendItem(color = Muted, borderColor = BorderColor, label = "가능")
            SeatLegendItem(color = MutedForeground.copy(alpha = 0.3f), label = "판매됨")
            SeatLegendItem(color = Warning.copy(alpha = 0.3f), label = "잠김")
            Spacer(Modifier.weight(1f))
            Text(
                text = "${selectedSeats.size}/$maxSeats",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedSeats.size >= maxSeats) Danger else Primary,
            )
        }

        HorizontalDivider(color = BorderColor)

        // Seat grid area (scrollable)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // STAGE label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(PrimaryDark, Primary)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "STAGE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 2.sp,
                    )
                }
            }

            // Seat grid
            val rows = seats.groupBy { it.row }.toSortedMap()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rows.forEach { (rowLabel, rowSeats) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // Row label
                        Text(
                            text = rowLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedForeground,
                            modifier = Modifier.width(20.dp),
                            textAlign = TextAlign.Center,
                        )

                        rowSeats.sortedBy { it.number }.forEach { seat ->
                            val isSelected = selectedSeats.any { it.id == seat.id }
                            SeatItem(
                                seat = seat,
                                isSelected = isSelected,
                                onClick = { onToggleSeat(seat) },
                            )
                        }

                        // Row label (right)
                        Text(
                            text = rowLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedForeground,
                            modifier = Modifier.width(20.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Bottom panel - Selected seats & payment button
        AnimatedVisibility(
            visible = selectedSeats.isNotEmpty(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            Surface(
                shadowElevation = 8.dp,
                color = CardBg,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    // Selected seats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        selectedSeats.forEach { seat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PrimaryLight,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "${seat.row}${seat.number}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PrimaryDark,
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "제거",
                                        tint = PrimaryDark,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .clickable { onToggleSeat(seat) },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Total price & payment button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "총 금액",
                                fontSize = 12.sp,
                                color = SubText,
                            )
                            Text(
                                text = "${numberFormat.format(selectedSeats.sumOf { it.price })} SSF",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                            )
                        }

                        Button(
                            onClick = onNavigateToPayment,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = White,
                            ),
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(
                                text = "결제하기 (${selectedSeats.size}매)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatItem(
    seat: Seat,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isSelected -> Primary
        seat.status == SeatStatus.AVAILABLE -> Muted
        seat.status == SeatStatus.SOLD -> MutedForeground.copy(alpha = 0.3f)
        seat.status == SeatStatus.LOCKED -> Warning.copy(alpha = 0.3f)
        else -> MutedForeground.copy(alpha = 0.3f)
    }

    val borderMod = if (seat.status == SeatStatus.AVAILABLE && !isSelected) {
        Modifier.border(1.dp, BorderColor, RoundedCornerShape(4.dp))
    } else {
        Modifier
    }

    val isClickable = seat.status == SeatStatus.AVAILABLE

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(borderMod)
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "선택됨",
                tint = White,
                modifier = Modifier.size(16.dp),
            )
        } else if (seat.status == SeatStatus.AVAILABLE) {
            Text(
                text = "${seat.number}",
                fontSize = 9.sp,
                color = MutedForeground,
            )
        }
    }
}

@Composable
private fun SeatLegendItem(
    color: Color,
    label: String,
    borderColor: Color? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(
                    if (borderColor != null) Modifier.border(1.dp, borderColor, RoundedCornerShape(3.dp))
                    else Modifier
                )
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MutedForeground,
        )
    }
}

private fun gradeToColor(name: String): Color {
    return when {
        name.contains("VIP", ignoreCase = true) || name.contains("PIT", ignoreCase = true) -> GradeVip
        name.contains("R") -> GradeR
        name.contains("S") -> GradeS
        name.contains("A") -> GradeA
        else -> GradeDefault
    }
}

