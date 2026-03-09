package com.ssafy.cheket.features.purchase

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    eventId: String,
    onSuccess: () -> Unit,
    onFailure: (eventId: String, reason: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.factory(eventId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    when (uiState.step) {
        PaymentStep.SUCCESS -> {
            PaymentSuccessContent(
                uiState = uiState,
                numberFormat = numberFormat,
                onViewTickets = onSuccess,
            )
        }
        PaymentStep.FAILURE -> {
            LaunchedEffect(Unit) {
                onFailure(eventId, uiState.failureReason)
            }
        }
        else -> {
            PaymentMainContent(
                uiState = uiState,
                numberFormat = numberFormat,
                onApprove = viewModel::approve,
                onConfirmPurchase = viewModel::confirmPurchase,
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
    onApprove: () -> Unit,
    onConfirmPurchase: () -> Unit,
    onToggleFailure: () -> Unit,
    onBack: () -> Unit,
) {
    val event = uiState.event ?: return

    Scaffold(
        topBar = {
            AppHeader(title = "결제 확인", onBack = onBack)
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                ) {
                    when (uiState.step) {
                        PaymentStep.REVIEW -> {
                            Button(
                                onClick = onApprove,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    contentColor = White,
                                ),
                                enabled = !uiState.isProcessing,
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(
                                        color = White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Approve", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        PaymentStep.APPROVED -> {
                            Button(
                                onClick = onConfirmPurchase,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    contentColor = White,
                                ),
                                enabled = !uiState.isProcessing,
                            ) {
                                if (uiState.isProcessing) {
                                    CircularProgressIndicator(
                                        color = White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("구매 확정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Step indicator
            StepIndicator(currentStep = uiState.step)

            // Event summary card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBg,
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = event.poster,
                        contentDescription = event.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 60.dp, height = 80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Muted),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = SubText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(event.date, fontSize = 12.sp, color = MutedForeground)
                        }
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = SubText, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(event.venue, fontSize = 12.sp, color = MutedForeground)
                        }
                    }
                }
            }

            // Selected seats detail
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBg,
                shadowElevation = 1.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EventSeat, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("선택 좌석", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(Modifier.height(12.dp))

                    uiState.selectedSeats.forEachIndexed { index, seat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryLight),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDark,
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${seat.grade} ${seat.row} ${seat.number}번",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = OnBackground,
                                    )
                                }
                            }
                            Text(
                                text = "${numberFormat.format(seat.price)}원",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground,
                            )
                        }
                        if (index < uiState.selectedSeats.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = BorderColor,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = OnBackground.copy(alpha = 0.2f),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("합계", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        Text(
                            text = "${numberFormat.format(uiState.totalPrice)}원",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }

            // CTK Balance card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBg,
                shadowElevation = 1.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("보유 CTK 잔액", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("보유 잔액", fontSize = 13.sp, color = MutedForeground)
                        Text(
                            text = "${numberFormat.format(uiState.user.ctkBalance)} CTK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("결제 금액", fontSize = 13.sp, color = MutedForeground)
                        Text(
                            text = "-${numberFormat.format(uiState.totalPrice)} CTK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Danger,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("결제 후 잔액", fontSize = 13.sp, color = MutedForeground)
                        val remaining = uiState.user.ctkBalance.toLong() - uiState.totalPrice.toLong()
                        Text(
                            text = "${numberFormat.format(remaining)} CTK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (remaining >= 0) Success else Danger,
                        )
                    }
                }
            }

            // Approved state indicator
            if (uiState.step == PaymentStep.APPROVED) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Success.copy(alpha = 0.1f),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "승인 완료",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Success,
                            )
                            Text(
                                text = "구매 확정 버튼을 눌러 결제를 완료하세요.",
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }

            // Dev toggle for failure simulation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Warning.copy(alpha = 0.08f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Warning.copy(alpha = 0.3f)),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "실패 시뮬레이션",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Warning,
                        )
                        Text(
                            text = "활성화 시 결제가 실패합니다",
                            fontSize = 11.sp,
                            color = MutedForeground,
                        )
                    }
                    Switch(
                        checked = uiState.simulateFailure,
                        onCheckedChange = { onToggleFailure() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Warning,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = BorderColor,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: PaymentStep) {
    val steps = listOf("승인" to PaymentStep.REVIEW, "확정" to PaymentStep.APPROVED)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { index, (label, step) ->
            val isActive = currentStep.ordinal >= step.ordinal
            val isCompleted = currentStep.ordinal > step.ordinal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Primary else BorderColor),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) White else MutedForeground,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) Primary else MutedForeground,
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .background(if (currentStep.ordinal > step.ordinal) Primary else BorderColor)
                )
            }
        }
    }
}

// --- Success Screen ---

@Composable
private fun PaymentSuccessContent(
    uiState: PaymentUiState,
    numberFormat: NumberFormat,
    onViewTickets: () -> Unit,
) {
    val event = uiState.event ?: return

    Scaffold(
        topBar = {
            AppHeader(title = "결제 완료")
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            // Success icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Success.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "성공",
                    tint = Success,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "예매가 완료되었습니다",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "구매한 티켓은 내 티켓에서 바로 확인할 수 있습니다.",
                fontSize = 14.sp,
                color = MutedForeground,
            )

            Spacer(Modifier.height(28.dp))

            // Event info card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardBg,
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = event.poster,
                            contentDescription = event.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 50.dp, height = 68.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Muted),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = event.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${event.date} | ${event.venue}",
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = BorderColor,
                    )

                    // Purchased seats summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ConfirmationNumber,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("구매 좌석", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                    }
                    Spacer(Modifier.height(8.dp))

                    uiState.selectedSeats.forEach { seat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${seat.grade} ${seat.row} ${seat.number}번",
                                fontSize = 13.sp,
                                color = MutedForeground,
                            )
                            Text(
                                text = "${numberFormat.format(seat.price)}원",
                                fontSize = 13.sp,
                                color = OnBackground,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BorderColor,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("총 결제 금액", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        Text(
                            text = "${numberFormat.format(uiState.totalPrice)}원",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // View tickets button
            Button(
                onClick = onViewTickets,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = White,
                ),
            ) {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "내 티켓 보기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
