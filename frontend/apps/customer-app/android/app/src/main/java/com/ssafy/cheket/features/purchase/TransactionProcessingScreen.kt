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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

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
    var txAmount by remember { mutableStateOf<Long?>(null) }
    var description by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Elapsed time counter
    LaunchedEffect(Unit) {
        while (currentStatus == TxStatus.PENDING || currentStatus == TxStatus.SUBMITTED) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // 실제 API 폴링 — GET /api/v1/wallets/transactions/{txId}
    val context = LocalContext.current
    val walletService = remember {
        (context.applicationContext as CheketApplication).appContainer.walletService
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
                val response = walletService.getTransactionStatus(txId)
                val data = response.data ?: continue

                val statusStr = data.resolvedStatus
                val hash = data.txHash
                val amount = data.amount
                val desc = data.description

                Log.d(TAG, "TX poll: status=$statusStr, txHash=$hash, amount=$amount, desc=$desc")

                // amount/description은 항상 최신값으로 갱신
                if (amount != null && amount > 0) txAmount = amount
                if (!desc.isNullOrBlank()) description = desc

                when (statusStr) {
                    "PENDING" -> {
                        currentStatus = TxStatus.PENDING
                    }
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
                        errorMessage = if (!desc.isNullOrBlank()) desc
                        else "블록체인 처리에 실패했습니다"
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
            delay(2000L)
            onComplete()
        } else if (currentStatus == TxStatus.FAILED) {
            // FAILED는 자동 이동하지 않음 — 사용자가 직접 선택
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
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                when (currentStatus) {
                    TxStatus.PENDING, TxStatus.SUBMITTED -> ProcessingContent(
                        status = currentStatus,
                        txHash = txHash,
                        txAmount = txAmount,
                        description = description,
                        elapsedSeconds = elapsedSeconds,
                        txType = txType,
                    )
                    TxStatus.CONFIRMED -> ConfirmedContent(
                        txHash = txHash,
                        txAmount = txAmount,
                        description = description,
                        txType = txType,
                    )
                    TxStatus.FAILED -> FailedContent(
                        errorMessage = errorMessage ?: "트랜잭션 처리에 실패했습니다",
                        txHash = txHash,
                        onRetry = {
                            currentStatus = TxStatus.PENDING
                            elapsedSeconds = 0
                            txHash = null
                            txAmount = null
                            description = null
                            errorMessage = null
                        },
                        onBack = onBack,
                    )
                }
            }
        }
    }
}

// ─── Processing (PENDING / SUBMITTED) ─────────────────────────────

@Composable
private fun ProcessingContent(
    status: TxStatus,
    txHash: String?,
    txAmount: Long?,
    description: String?,
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
        // Spinning progress ring with elapsed time
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                val stroke = 6.dp.toPx()
                val radius = (size.minDimension - stroke) / 2f
                val topLeft = Offset(stroke / 2f, stroke / 2f)
                val arcSize = Size(radius * 2f, radius * 2f)

                drawArc(
                    color = ProgressTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
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

        // 메인 상태 텍스트 — description 우선, 없으면 status 기반 매핑
        Text(
            text = description ?: when (status) {
                TxStatus.PENDING -> "SSF 승인 처리 중..."
                TxStatus.SUBMITTED -> "블록체인에 기록 중..."
                else -> ""
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = V0Fg,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "${txTypeLabel(txType)} 처리를 진행하고 있어요.\n잠시만 기다려 주세요.",
            fontSize = 14.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
    }

    // 진행 단계 카드
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusStepItem(
            stepNumber = 1,
            label = "SSF 승인",
            description = "토큰 사용 승인 서명",
            isCompleted = status != TxStatus.PENDING,
            isActive = status == TxStatus.PENDING,
        )
        StatusStepItem(
            stepNumber = 2,
            label = "블록체인 전송",
            description = if (txHash != null) "TX: ${txHash.take(10)}...${txHash.takeLast(6)}"
            else "스마트 컨트랙트 호출 대기",
            isCompleted = false,
            isActive = status == TxStatus.SUBMITTED,
        )
        StatusStepItem(
            stepNumber = 3,
            label = "블록 확정",
            description = "블록에 포함 및 NFT 발행",
            isCompleted = false,
            isActive = false,
        )
    }

    // 하단 정보
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (txAmount != null && txAmount > 0) {
            Text(
                text = "결제 금액: ${formatAmount(txAmount)} CTK",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = V0Fg,
            )
        }
        Text(
            text = "화면을 닫지 마세요. 처리가 완료되면 자동으로 이동합니다.",
            fontSize = 12.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Confirmed ────────────────────────────────────────────────────

@Composable
private fun ConfirmedContent(
    txHash: String?,
    txAmount: Long?,
    description: String?,
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
            text = "${txTypeLabel(txType)} 완료!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = V0Fg,
        )

        // description 있으면 표시 (예: "구매 완료 (2매, 200 SSF)")
        Text(
            text = description ?: "${txTypeLabel(txType)}이(가) 블록체인에 기록되었습니다.",
            fontSize = 14.sp,
            color = V0Muted,
            textAlign = TextAlign.Center,
        )
    }

    // TX 상세 정보 카드
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TxInfoRow("상태", "CONFIRMED", ConfirmedGreen)

        if (txHash != null) {
            TxInfoRow("TX Hash", "${txHash.take(10)}...${txHash.takeLast(6)}")
        }

        if (txAmount != null && txAmount > 0) {
            TxInfoRow("결제 금액", "${formatAmount(txAmount)} CTK")
        }
    }

    Text(
        text = "잠시 후 자동으로 이동합니다...",
        fontSize = 12.sp,
        color = V0Muted,
        textAlign = TextAlign.Center,
    )
}

// ─── Failed ───────────────────────────────────────────────────────

@Composable
private fun FailedContent(
    errorMessage: String,
    txHash: String?,
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

    // 실패 시에도 txHash가 있으면 표시
    if (txHash != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .elevatedSurface()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TxInfoRow("상태", "FAILED", FailedRed)
            TxInfoRow("TX Hash", "${txHash.take(10)}...${txHash.takeLast(6)}")
        }
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

// ─── Shared Components ────────────────────────────────────────────

@Composable
private fun StatusStepItem(
    stepNumber: Int,
    label: String,
    description: String,
    isCompleted: Boolean,
    isActive: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Step indicator circle
        Box(
            modifier = Modifier
                .size(32.dp)
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
                    modifier = Modifier.size(20.dp),
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "$stepNumber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
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
private fun TxInfoRow(
    label: String,
    value: String,
    valueColor: Color = V0Fg,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = V0Muted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────

private fun txTypeLabel(type: String): String = when (type) {
    "TICKET_PURCHASE" -> "티켓 구매"
    "RESALE_LIST" -> "리세일 등록"
    "RESALE_PURCHASE" -> "리세일 구매"
    "TRANSFER" -> "티켓 양도"
    "REFUND" -> "환불"
    else -> "트랜잭션"
}

private fun formatAmount(amount: Long): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
