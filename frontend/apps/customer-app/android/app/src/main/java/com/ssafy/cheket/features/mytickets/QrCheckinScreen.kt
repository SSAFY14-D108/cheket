package com.ssafy.cheket.features.mytickets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun QrCheckinScreen(
    ticketId: String,
    onBack: () -> Unit,
) {
    val ticket = remember(ticketId) {
        MockDataSource.mockTickets.find { it.id == ticketId }
    }

    var isCheckedIn by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(30) }
    var qrSeed by remember { mutableIntStateOf(Random.nextInt()) }

    // Countdown timer
    LaunchedEffect(qrSeed) {
        remainingSeconds = 30
        while (remainingSeconds > 0 && !isCheckedIn) {
            delay(1000)
            remainingSeconds--
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "QR 체크인",
                onBack = onBack,
                actions = {
                    TutorialHelpButton(tutorialId = TutorialId.QR_CHECKIN)
                },
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(visible = isCheckedIn, enter = fadeIn() + scaleIn()) {
                // --- Success State ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Success.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "체크인 완료",
                            tint = Success,
                            modifier = Modifier.size(56.dp),
                        )
                    }

                    Text(
                        "체크인 완료!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )

                    Text(
                        ticket?.showName ?: "",
                        fontSize = 14.sp,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("닫기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            if (!isCheckedIn) {
                // --- Event info ---
                Text(
                    text = ticket?.showName ?: "알 수 없는 공연",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = ticket?.seatLabel ?: "",
                    fontSize = 13.sp,
                    color = MutedForeground,
                )

                // --- QR Code with Countdown Ring ---
                Box(contentAlignment = Alignment.Center) {
                    // Circular progress ring
                    val progress = remainingSeconds / 30f
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val topLeft = Offset(
                            (size.width - radius * 2) / 2f,
                            (size.height - radius * 2) / 2f,
                        )

                        // Track
                        drawArc(
                            color = BorderColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        // Progress
                        val progressColor = when {
                            remainingSeconds <= 5 -> Color(0xFFE53E3E)
                            remainingSeconds <= 10 -> Color(0xFFF59E0B)
                            else -> Color(0xFF00C598)
                        }
                        drawArc(
                            color = progressColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    // QR Placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MockQrCode(seed = qrSeed, modifier = Modifier.size(160.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${remainingSeconds}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds <= 5) Danger else OnBackground,
                        )
                    }
                }

                // Always show status text per v0-version2
                Text(
                    if (remainingSeconds > 0) "QR 코드가 자동 갱신됩니다"
                    else "QR 코드가 만료되었습니다. 재발급 버튼을 눌러주세요.",
                    fontSize = 13.sp,
                    color = if (remainingSeconds > 0) MutedForeground else Danger,
                    textAlign = TextAlign.Center,
                )

                // --- Refresh button ---
                OutlinedButton(
                    onClick = {
                        qrSeed = Random.nextInt()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("QR 재발급", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                // --- Mock Check-in button ---
                Button(
                    onClick = { isCheckedIn = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = remainingSeconds > 0,
                ) {
                    Text("체크인 (Mock)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * A simple deterministic mock QR code rendered as a grid of black and white cells.
 */
@Composable
private fun MockQrCode(seed: Int, modifier: Modifier = Modifier) {
    val gridSize = 21
    val cells = remember(seed) {
        val random = Random(seed)
        Array(gridSize) { row ->
            BooleanArray(gridSize) { col ->
                // Keep corners for finder patterns
                val isFinderArea = (row < 3 && col < 3) ||
                        (row < 3 && col >= gridSize - 3) ||
                        (row >= gridSize - 3 && col < 3)
                if (isFinderArea) true else random.nextBoolean()
            }
        }
    }

    Canvas(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(White)) {
        val cellSize = size.minDimension / gridSize
        val padding = 8.dp.toPx()
        val drawableArea = size.minDimension - padding * 2
        val drawCellSize = drawableArea / gridSize

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (cells[row][col]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(padding + col * drawCellSize, padding + row * drawCellSize),
                        size = Size(drawCellSize, drawCellSize),
                    )
                }
            }
        }
    }
}
