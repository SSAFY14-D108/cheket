package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.CardBg
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.SubText
import com.ssafy.cheket.ui.theme.Warning
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun QrCheckinScreen(
    ticketId: String,
    onBack: () -> Unit,
    viewModel: QrCheckinViewModel = viewModel(factory = QrCheckinViewModel.factory(ticketId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingSeconds = uiState.expiresIn

    Scaffold(
        topBar = {
            AppHeader(
                title = "QR 코드 보기",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = uiState.title.ifBlank { "티켓 정보를 확인하는 중입니다." },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )

            if (uiState.seatLabel.isNotBlank()) {
                Text(
                    text = uiState.seatLabel,
                    fontSize = 13.sp,
                    color = MutedForeground,
                )
            }

            if (uiState.isLoading && uiState.qrData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    val progress = (remainingSeconds.coerceAtLeast(0) / 30f).coerceIn(0f, 1f)

                    Canvas(modifier = Modifier.size(240.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2f
                        val topLeft = Offset(
                            (size.width - radius * 2) / 2f,
                            (size.height - radius * 2) / 2f,
                        )

                        drawArc(
                            color = BorderColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        drawArc(
                            color = when {
                                remainingSeconds <= 5 -> Danger
                                remainingSeconds <= 10 -> Warning
                                else -> Primary
                            },
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (uiState.qrData != null) {
                            TokenMatrixCode(
                                payload = uiState.qrData ?: ticketId,
                                modifier = Modifier.size(160.dp),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardBg),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MutedForeground,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (remainingSeconds > 0) "${remainingSeconds}s" else "만료됨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds <= 5) Danger else OnBackground,
                        )
                    }
                }

                Text(
                    text = when {
                        uiState.qrData == null -> "QR 코드를 불러오지 못했습니다."
                        remainingSeconds > 0 -> "유효 시간이 지나면 새 QR을 다시 받아야 합니다."
                        else -> "QR 코드가 만료되었습니다. 새로고침해서 다시 발급해주세요."
                    },
                    fontSize = 13.sp,
                    color = if (remainingSeconds > 0 && uiState.qrData != null) MutedForeground else Danger,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "30초마다 자동으로 새 QR이 발급됩니다.",
                    fontSize = 12.sp,
                    color = SubText,
                )
            }

            uiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.08f)),
                ) {
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = OnBackground,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            OutlinedButton(
                onClick = viewModel::refreshQr,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                enabled = !uiState.isRefreshing,
            ) {
                if (uiState.isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Primary,
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (uiState.isRefreshing) "다시 불러오는 중..." else "QR 새로고침",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }

            uiState.qrData?.let { qrData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("입장 코드", fontSize = 12.sp, color = MutedForeground)
                        Text(
                            text = qrData,
                            fontSize = 13.sp,
                            color = OnBackground,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TokenMatrixCode(
    payload: String,
    modifier: Modifier = Modifier,
) {
    val matrix = remember(payload) {
        try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            writer.encode(payload, com.google.zxing.BarcodeFormat.QR_CODE, 256, 256)
        } catch (e: Exception) {
            null
        }
    }

    if (matrix != null) {
        Canvas(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(White)) {
            val padding = 8.dp.toPx()
            val drawableArea = size.minDimension - padding * 2
            val cellWidth = drawableArea / matrix.width
            val cellHeight = drawableArea / matrix.height

            for (y in 0 until matrix.height) {
                for (x in 0 until matrix.width) {
                    if (matrix.get(x, y)) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(padding + x * cellWidth, padding + y * cellHeight),
                            size = Size(cellWidth, cellHeight),
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = modifier.clip(RoundedCornerShape(8.dp)).background(CardBg),
            contentAlignment = Alignment.Center,
        ) {
            Text("QR 생성 실패", fontSize = 12.sp, color = MutedForeground)
        }
    }
}

