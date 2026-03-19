package com.ssafy.cheket.features.purchase

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.People
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
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay

// v0 exact colors
private val ProgressStroke = Color(0xFF9AA4B2)
private val TrackStroke = Color(0xFFE5E7EB)
private val ProgressTrackBg = Color(0xFFEEF1F4)
private val V0Fg = Color(0xFF111111)
private val V0Muted = Color(0xFF6B7280)
private val V0GradientBg = Color(0xFFF9FAFB)

private const val TOTAL_WAIT_SECONDS = 5
private const val MOCK_QUEUE_POSITION = 47
private const val MOCK_ESTIMATED_WAIT = 3

private enum class QueueState { WAITING, READY_TO_ENTER, EXPIRED }

@Composable
fun WaitingQueueScreen(
    showId: String,
    showName: String = "",
    showDate: String = "",
    onComplete: (showId: String) -> Unit,
    onBack: () -> Unit,
) {
    var queueState by remember { mutableStateOf(QueueState.WAITING) }
    var position by remember { mutableIntStateOf(MOCK_QUEUE_POSITION) }
    var elapsed by remember { mutableIntStateOf(0) }
    var readyCountdown by remember { mutableIntStateOf(60) }

    val progress by animateFloatAsState(
        targetValue = (elapsed.toFloat() / TOTAL_WAIT_SECONDS).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "progress",
    )

    // WAITING countdown
    LaunchedEffect(queueState) {
        if (queueState != QueueState.WAITING) return@LaunchedEffect
        for (sec in 1..TOTAL_WAIT_SECONDS) {
            delay(1000L)
            elapsed = sec
            position = (position - (1..4).random()).coerceAtLeast(1)
        }
        queueState = QueueState.READY_TO_ENTER
    }

    // READY auto-navigate
    LaunchedEffect(queueState) {
        if (queueState != QueueState.READY_TO_ENTER) return@LaunchedEffect
        delay(1500L)
        onComplete(showId)
    }

    // READY countdown
    LaunchedEffect(queueState) {
        if (queueState != QueueState.READY_TO_ENTER) return@LaunchedEffect
        while (readyCountdown > 0) {
            delay(1000L)
            readyCountdown--
        }
        queueState = QueueState.EXPIRED
    }

    val estimatedWait = (MOCK_ESTIMATED_WAIT * (1f - progress)).toInt().coerceAtLeast(0)

    Scaffold(
        topBar = { AppHeader(title = "대기열", onBack = onBack) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(V0GradientBg)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                when (queueState) {
                    QueueState.WAITING -> WaitingContent(
                        progress = progress,
                        remaining = (TOTAL_WAIT_SECONDS - elapsed).coerceAtLeast(0),
                        position = position,
                        estimatedWait = estimatedWait,
                        showName = showName,
                    )
                    QueueState.READY_TO_ENTER -> ReadyContent(
                        readyCountdown = readyCountdown,
                        showName = showName,
                        showDate = showDate,
                        onEnter = { onComplete(showId) },
                    )
                    QueueState.EXPIRED -> ExpiredContent(
                        onRetry = {
                            queueState = QueueState.WAITING
                            elapsed = 0
                            position = MOCK_QUEUE_POSITION
                            readyCountdown = 60
                        },
                        onBack = onBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitingContent(
    progress: Float,
    remaining: Int,
    position: Int,
    estimatedWait: Int,
    showName: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // SVG-style progress ring (112x112 = 28*4dp ≈ 112dp)
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 6.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val topLeft = Offset(stroke / 2f, stroke / 2f)
                val arcSize = Size(radius * 2f, radius * 2f)

                // Track ring
                drawArc(
                    color = TrackStroke,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // Progress arc
                drawArc(
                    color = ProgressStroke,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = V0Muted,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$remaining",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0Fg,
                )
                Text(
                    text = "초 남음",
                    fontSize = 12.sp,
                    color = V0Muted,
                )
            }
        }

        // Queue position
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("현재 대기 순번", fontSize = 14.sp, color = V0Muted)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$position",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = V0Fg,
            )
            Text("번째", fontSize = 14.sp, color = V0Muted)
        }
    }

    // Info card — elevated-surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.People, null, tint = V0Muted, modifier = Modifier.size(16.dp))
                Text("예상 대기 시간", fontSize = 14.sp, color = V0Muted)
            }
            Text(
                text = if (estimatedWait == 0) "곧 입장 가능" else "약 ${estimatedWait}분",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0Fg,
            )
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(ProgressTrackBg),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(50))
                    .background(ProgressStroke),
            )
        }

        Text(
            text = showName,
            fontSize = 12.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Text(
        text = "순서가 되면 자동으로 좌석 선택 화면으로 이동해요.\n화면을 유지한 상태로 잠시만 기다려 주세요.",
        fontSize = 12.sp,
        color = V0Muted,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
    )
}

@Composable
private fun ReadyContent(
    readyCountdown: Int,
    showName: String,
    showDate: String,
    onEnter: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Check circle with gradient border
        Box(
            modifier = Modifier
                .size(96.dp)
                .gradientBorder(shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF333333),
                modifier = Modifier.size(48.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "입장할 수 있어요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = V0Fg,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "좌석 선택 화면으로 바로 이동할 수 있어요.",
                fontSize = 14.sp,
                color = V0Muted,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "${readyCountdown}초 안에 입장해 주세요.",
                fontSize = 14.sp,
                color = V0Muted,
                textAlign = TextAlign.Center,
            )
        }
    }

    // Show info card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(showName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = V0Fg)
        if (showDate.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(showDate, fontSize = 12.sp, color = V0Muted)
        }
    }

    // Enter button — gradient-border-button
    Text(
        text = "바로 입장하기",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = V0Fg,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .gradientBorder(shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 16.dp),
    )
}

@Composable
private fun ExpiredContent(
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF2F2))
                .padding(1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(48.dp),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "입장 시간이 만료됐어요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = V0Fg,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "다시 대기열에 등록한 뒤 순서를 기다려야 해요.",
                fontSize = 14.sp,
                color = V0Muted,
                textAlign = TextAlign.Center,
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Retry button — gradient-border
        Text(
            text = "다시 대기하기",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Fg,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .gradientBorder(shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickableNoRipple(onClick = onRetry)
                .padding(vertical = 16.dp),
        )
        // Back button — elevated-surface
        Text(
            text = "이전 화면으로",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Fg,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .elevatedSurface()
                .clickableNoRipple(onClick = onBack)
                .padding(vertical = 14.dp),
        )
    }
}

@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )
