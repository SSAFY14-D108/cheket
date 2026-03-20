package com.ssafy.cheket.features.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// ── v0 design tokens ──
private val V0Bg = Color(0xFFF8F8F8)
private val V0Text = Color(0xFF111111)
private val V0Muted = Color(0xFF6B7280)
private val V0Border = Color(0xFFE5E7EB)
private val V0Red500 = Color(0xFFEF4444)
private val V0SwitchTrack = Color(0xFF9AA4B2)

@Composable
fun PaymentScreen(
    showId: String,
    onSuccess: (txId: Long) -> Unit,
    onFailure: (showId: String, reason: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.factory(showId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    when (uiState.step) {
        PaymentStep.SUCCESS -> {
            LaunchedEffect(Unit) { onSuccess(uiState.txId ?: 0L) }
        }
        PaymentStep.FAILURE -> {
            LaunchedEffect(Unit) { onFailure(showId, uiState.failureReason) }
        }
        else -> {
            PaymentMainContent(
                uiState = uiState,
                numberFormat = numberFormat,
                onPurchase = viewModel::purchase,
                onToggleFailure = viewModel::toggleFailureSimulation,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun PaymentMainContent(
    uiState: PaymentUiState,
    numberFormat: NumberFormat,
    onPurchase: () -> Unit,
    onToggleFailure: () -> Unit,
    onBack: () -> Unit,
) {
    if (uiState.isLoading) {
        Scaffold(topBar = { AppHeader(title = "결제 확인", onBack = onBack) }) { p ->
            Box(
                Modifier.fillMaxSize().background(V0Bg).padding(p),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = V0Muted)
            }
        }
        return
    }

    val show = uiState.show ?: run {
        Scaffold(topBar = { AppHeader(title = "결제 확인", onBack = onBack) }) { p ->
            Box(
                Modifier.fillMaxSize().background(V0Bg).padding(p),
                contentAlignment = Alignment.Center,
            ) {
                Text("공연 정보를 불러올 수 없습니다.", color = V0Muted, fontSize = 14.sp)
            }
        }
        return
    }

    val hasSufficientBalance = uiState.user.ctkBalance >= uiState.totalPrice
    val canPurchase = uiState.step == PaymentStep.REVIEW && !uiState.isProcessing && hasSufficientBalance

    Scaffold(
        topBar = { AppHeader(title = "결제 확인", onBack = onBack) },
        containerColor = V0Bg,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V0Bg)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── 1. 공연명 + 좌석 + 합계 카드 (v0: elevated-surface rounded-xl p-4) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurface(RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = show.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0Text,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )

                    uiState.selectedSeats.forEach { seat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row {
                                Text(
                                    text = "${seat.row}-${seat.number}번",
                                    fontSize = 14.sp,
                                    color = V0Text,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "(${seat.grade})",
                                    fontSize = 12.sp,
                                    color = V0Muted,
                                )
                            }
                            Text(
                                text = "${numberFormat.format(seat.price)} CTK",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = V0Text,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = V0Border,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "총 결제 금액",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Text,
                        )
                        Text(
                            "${numberFormat.format(uiState.totalPrice)} CTK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = V0Text,
                        )
                    }
                }

                // ── 2. 보유 CTK 잔액 카드 (v0: elevated-surface or red border when insufficient) ──
                val balanceModifier = if (hasSufficientBalance) {
                    Modifier
                        .fillMaxWidth()
                        .elevatedSurface(RoundedCornerShape(12.dp))
                } else {
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2)) // red-50
                        .then(
                            Modifier.padding(0.dp) // border handled below
                        )
                }

                Column(
                    modifier = balanceModifier.padding(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp),
                    ) {
                        Icon(
                            Icons.Outlined.AccountBalanceWallet,
                            null,
                            tint = if (hasSufficientBalance) V0Muted else V0Red500,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "보유 CTK 잔액",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Text,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = numberFormat.format(uiState.user.ctkBalance),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = V0Text,
                        )
                        Text(
                            "CTK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = V0Muted,
                        )
                    }

                    if (!hasSufficientBalance) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.ErrorOutline,
                                null,
                                tint = V0Red500,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "잔액이 부족해요. ${numberFormat.format(uiState.totalPrice - uiState.user.ctkBalance)} CTK 이상 충전한 뒤 다시 시도해 주세요.",
                                fontSize = 12.sp,
                                color = V0Red500,
                            )
                        }
                    }
                }

                // ── 3. 안내 문구 + 결제 진행 버튼 ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurface(RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        "결제를 진행하면 선택한 좌석이 확정되고 티켓이 즉시 발급돼요.",
                        fontSize = 12.sp,
                        color = V0Muted,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                // ── 4. 테스트 실패 시뮬레이션 토글 (v0: elevated-surface-soft) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurfaceSoft(RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            Icons.Outlined.Science,
                            null,
                            tint = V0Muted,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "테스트용 실패 시뮬레이션",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Muted,
                        )
                    }
                    Switch(
                        checked = uiState.simulateFailure,
                        onCheckedChange = { onToggleFailure() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = V0SwitchTrack,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = V0Muted.copy(alpha = 0.3f),
                        ),
                    )
                }

                // ── 5. 결제 진행하기 CTA (v0: gradient-border-button) ──
                Button(
                    onClick = onPurchase,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .then(
                            if (canPurchase) Modifier.gradientBorder(RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = V0Text,
                        disabledContainerColor = V0Border,
                        disabledContentColor = V0Muted,
                    ),
                    enabled = canPurchase,
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                ) {
                    if (uiState.isProcessing || uiState.step == PaymentStep.PROCESSING) {
                        CircularProgressIndicator(
                            color = V0Text,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("처리 중...", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text(
                            if (uiState.simulateFailure) "실패 케이스로 진행" else "결제 진행하기",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                // ── 6. 하단 안내 (v0) ──
                Text(
                    "결제가 완료되면 선택한 좌석 정보가 NFT 티켓으로 발급되고, 내 티켓에서 바로 확인할 수 있어요.",
                    fontSize = 12.sp,
                    color = V0Muted,
                    modifier = Modifier.padding(vertical = 4.dp),
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
