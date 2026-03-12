package com.ssafy.cheket.features.purchase

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WaitingQueueScreen(
    showId: String,
    onComplete: (showId: String) -> Unit,
    onBack: () -> Unit,
) {
    val totalSeconds = 5
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }
    var queuePosition by remember { mutableIntStateOf(47) }

    val progress by animateFloatAsState(
        targetValue = 1f - (remainingSeconds.toFloat() / totalSeconds),
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "progress",
    )

    // Countdown timer
    LaunchedEffect(Unit) {
        for (sec in totalSeconds downTo 1) {
            remainingSeconds = sec
            // Decrease queue position randomly
            val decrease = (3..10).random()
            queuePosition = (queuePosition - decrease).coerceAtLeast(0)
            delay(1000L)
        }
        remainingSeconds = 0
        queuePosition = 0
        delay(500L)
        onComplete(showId)
    }

    // Spinning animation for the ring
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Scaffold(
        topBar = {
            AppHeader(title = "대기열", onBack = onBack)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                // Circular progress indicator
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Background ring
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = BorderColor,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    // Progress ring
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotation),
                        color = Primary,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    // Inner content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${remainingSeconds}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Primary,
                        )
                        Text(
                            text = "초 남음",
                            fontSize = 14.sp,
                            color = MutedForeground,
                        )
                    }
                }

                // Queue info card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg,
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "현재 대기 순번",
                            fontSize = 13.sp,
                            color = SubText,
                        )
                        Text(
                            text = "${queuePosition}번째",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = Muted,
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }

                // Info text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp),
                ) {
                    Text(
                        text = "잠시만 기다려 주세요",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "순서가 되면 자동으로 좌석 선택 화면으로 이동합니다.\n화면을 닫지 말고 잠시만 기다려 주세요.",
                        fontSize = 13.sp,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }

                // Dots animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 200),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "dot_$index",
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = dotAlpha))
                        )
                    }
                }
            }
        }
    }
}
