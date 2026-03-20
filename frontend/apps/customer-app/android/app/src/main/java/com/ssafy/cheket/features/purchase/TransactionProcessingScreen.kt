package com.ssafy.cheket.features.purchase

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.service.TicketService
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay

private const val TAG = "TxProcessingScreen"

// v0 colors
private val V0Fg = Color(0xFF111111)
private val V0Muted = Color(0xFF6B7280)
private val V0GradientBg = Color(0xFFF9FAFB)
private val ProgressBlue = Color(0xFF3B82F6)
private val ProgressTrack = Color(0xFFE5E7EB)
private val ConfirmedGreen = Color(0xFF10B981)
private val FailedRed = Color(0xFFF87171)

/**
 * 블록체인 TX 상태:
 * PENDING → SUBMITTED → CONFIRMED / FAILED
 */
private enum class TxStatus { PENDING, SUBMITTED, CONFIRMED, FAILED }

/**
 * TX 상태 응답 — GET /api/v1/tx/{txId}/status
 * 백엔드 응답: { txId, status, txHash, amount }
 */
private data class TxStatusResponse(
    val txId: Long,
    val status: String,
    val txHash: String?,
    val amount: Long?,
)

@Composable
fun TransactionProcessingScreen(
    txId: Long = 0,
    txType: String = "TICKET_PURCHASE",
    onComplete: () -> Unit,
    onFailure: (String) -> Unit,
    onBack: () -> Unit,
) {
    var currentStatus by remember { mutableStateOf(TxStatus.PENDING) }
    var txHash by remember { mutableStateOf<String?>(null) }
    var blockNumber by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Elapsed time counter
    LaunchedEffect(Unit) {
        while (currentStatus == TxStatus.PENDING || currentStatus == TxStatus.SUBMITTED) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // 실제 API 폴링 — GET /api/v1/tx/{txId}/status
    val context = androidx.compose.ui.platform.LocalContext.current
    val ticketService = remember {
        (context.applicationContext as CheketApplication).appContainer.ticketService
    }

    LaunchedEffect(txId) {
        if (txId <= 0L) {
            Log.w(TAG, "Invalid txId=$txId, skipping polling")
            return@LaunchedEffect
        }
        Log.d(TAG, "Starting TX polling for txId=$txId, type=$txType")

        while (currentStatus == TxStatus.PENDING || currentStatus == TxStatus.SUBMITTED) {
            delay(1500L)
            try {
                val response = ticketService.getTxStatus(txId)
                val data = response.data ?: continue

                val statusStr = (data["status"] as? String) ?: continue
                val hash = data["txHash"] as? String

                Log.d(TAG, "TX poll: status=$statusStr, txHash=$hash")

                when (statusStr) {
                    "SUBMITTED" -> {
                        currentStatus = TxStatus.SUBMITTED
                        if (!hash.isNullOrBlank()) txHash = hash
                    }
                    "CONFIRMED" -> {
                        currentStatus = TxStatus.CONFIRMED
                        if (!hash.isNullOrBlank()) txHash = hash
                    }
                    "FAILED" -> {
                        currentStatus = TxStatus.FAILED
                        errorMessage = "블록체인 처리에 실패했습니다"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TX poll failed", e)
                // 네트워크 오류는 무시하고 계속 폴링
            }
        }
    }

    // Auto-navigate on CONFIRMED
    LaunchedEffect(currentStatus) {
        if (currentStatus == TxStatus.CONFIRMED) {
            delay(1500L)
            onComplete()
        } else if (currentStatus == TxStatus.FAILED) {
            delay(1000L)
            onFailure(errorMessage ?: "트랜잭션 처리에 실패했습니다")
        }
    }

    // 뒤로가기 막기 (처리 중)
    androidx.activity.compose.BackHandler(
        enabled = currentStatus == TxStatus.PENDING || currentStatus == TxStatus.SUBMITTED
    ) {
        // 처리 중엔 뒤로가기 무시
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = txTypeLabel(txType),
                onBack = if (currentStatus == TxStatus.CONFIRMED || currentStatus == TxStatus.FAILED)
                    onBack else null,
            )
        },
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
                when (currentStatus) {
                    TxStatus.PENDING, TxStatus.SUBMITTED -> ProcessingContent(
                        status = currentStatus,
                        txHash = txHash,
                        elapsedSeconds = elapsedSeconds,
                        txType = txType,
                    )
                    TxStatus.CONFIRMED -> ConfirmedContent(
                        txHash = txHash,
                        blockNumber = blockNumber,
                        txType = txType,
                    )
                    TxStatus.FAILED -> FailedContent(
                        errorMessage = errorMessage ?: "트랜잭션 처리에 실패했습니다",
                        onRetry = {
                            currentStatus = TxStatus.PENDING
                            elapsedSeconds = 0
                            txHash = null
                            blockNumber = null
                            errorMessage = null
                        },
                        onBack = onBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingContent(
    status: TxStatus,
    txHash: String?,
    elapsedSeconds: Int,
    txType: String,
) {
    // Animated spinning ring
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    val progress by animateFloatAsState(
        targetValue = when (status) {
            TxStatus.PENDING -> 0.3f
            TxStatus.SUBMITTED -> 0.7f
            else -> 1f
        },
        animationSpec = tween(800),
        label = "progress",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Spinning progress ring
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                val stroke = 6.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val topLeft = Offset(stroke / 2f, stroke / 2f)
                val arcSize = Size(radius * 2f, radius * 2f)

                // Track
                drawArc(
                    color = ProgressTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                // Progress arc
                drawArc(
                    color = ProgressBlue,
                    startAngle = -90f,
                    sweepAngle = progress * 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${elapsedSeconds}s",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0Fg,
                )
                Text(
                    text = "경과",
                    fontSize = 11.sp,
                    color = V0Muted,
                )
            }
        }

        // Status text
        Text(
            text = when (status) {
                TxStatus.PENDING -> "트랜잭션 준비 중..."
                TxStatus.SUBMITTED -> "블록체인에 기록 중..."
                else -> ""
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = V0Fg,
        )

        Text(
            text = "${txTypeLabel(txType)} 처리를 진행하고 있어요.\n잠시만 기다려 주세요.",
            fontSize = 14.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
    }

    // Status steps card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusStepItem(
            label = "트랜잭션 생성",
            description = "서명 및 전송 준비",
            isCompleted = status != TxStatus.PENDING,
            isActive = status == TxStatus.PENDING,
        )
        StatusStepItem(
            label = "블록체인 전송",
            description = if (txHash != null) "TX: ${txHash!!.take(10)}...${txHash!!.takeLast(6)}" else "노드에 전송 대기",
            isCompleted = false,
            isActive = status == TxStatus.SUBMITTED,
        )
        StatusStepItem(
            label = "블록 확정",
            description = "블록에 포함 대기 중",
            isCompleted = false,
            isActive = false,
        )
    }

    Text(
        text = "화면을 닫지 마세요. 처리가 완료되면 자동으로 이동합니다.",
        fontSize = 12.sp,
        color = V0Muted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StatusStepItem(
    label: String,
    description: String,
    isCompleted: Boolean,
    isActive: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> ConfirmedGreen
                        isActive -> ProgressBlue
                        else -> ProgressTrack
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isActive || isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive || isCompleted) V0Fg else V0Muted,
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = V0Muted,
            )
        }

        if (isCompleted) {
            Text("완료", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ConfirmedGreen)
        } else if (isActive) {
            Text("처리 중", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ProgressBlue)
        }
    }
}

@Composable
private fun ConfirmedContent(
    txHash: String?,
    blockNumber: Long?,
    txType: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Success icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(ConfirmedGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = ConfirmedGreen,
                modifier = Modifier.size(56.dp),
            )
        }

        Text(
            text = "처리 완료!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = V0Fg,
        )

        Text(
            text = "${txTypeLabel(txType)}이(가) 성공적으로 처리되었습니다.",
            fontSize = 14.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
        )
    }

    // TX Info card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (txHash != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("TX Hash", fontSize = 13.sp, color = V0Muted)
                Text(
                    text = "${txHash.take(10)}...${txHash.takeLast(6)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = V0Fg,
                )
            }
        }
        if (blockNumber != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Block", fontSize = 13.sp, color = V0Muted)
                Text(
                    text = "#$blockNumber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = V0Fg,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("상태", fontSize = 13.sp, color = V0Muted)
            Text(
                text = "CONFIRMED",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ConfirmedGreen,
            )
        }
    }

    Text(
        text = "잠시 후 자동으로 이동합니다...",
        fontSize = 12.sp,
        color = V0Muted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FailedContent(
    errorMessage: String,
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
                .background(Color(0xFFFEF2F2)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = FailedRed,
                modifier = Modifier.size(56.dp),
            )
        }

        Text(
            text = "처리에 실패했어요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = V0Fg,
        )

        Text(
            text = errorMessage,
            fontSize = 14.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "다시 시도",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Fg,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .gradientBorder(shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(onClick = onRetry)
                .padding(vertical = 16.dp),
        )
        Text(
            text = "이전 화면으로",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Fg,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .elevatedSurface()
                .clickable(onClick = onBack)
                .padding(vertical = 14.dp),
        )
    }
}

private fun txTypeLabel(type: String): String = when (type) {
    "TICKET_PURCHASE" -> "티켓 구매"
    "RESALE_LIST" -> "리세일 등록"
    "RESALE_PURCHASE" -> "리세일 구매"
    "TRANSFER" -> "티켓 양도"
    else -> "트랜잭션"
}
