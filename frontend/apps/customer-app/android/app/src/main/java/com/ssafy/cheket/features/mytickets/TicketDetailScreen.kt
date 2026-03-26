package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.CheketAlertDialog
import com.ssafy.cheket.core.ui.component.CheketDialog
import com.ssafy.cheket.core.ui.component.TicketStatusBadge
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.CardBg
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.Warning
import com.ssafy.cheket.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TicketDetailScreen(
    ticketId: String,
    onQrCheckin: (String) -> Unit,
    onTransfer: (String) -> Unit,
    onResaleCreate: (String) -> Unit,
    onResaleCancelRequested: (Long) -> Unit,
    onRefundSuccess: (txId: Long?) -> Unit,
    onBack: () -> Unit,
    refundViewModel: TicketRefundViewModel = viewModel(factory = TicketRefundViewModel.Factory),
    resaleCancelViewModel: ResaleCancelViewModel = viewModel(factory = ResaleCancelViewModel.Factory),
) {
    val refundUiState by refundViewModel.uiState.collectAsStateWithLifecycle()
    val resaleCancelUiState by resaleCancelViewModel.uiState.collectAsStateWithLifecycle()
    val ticket = remember(ticketId) {
        NavParams.selectedTicket?.takeIf { it.id == ticketId }
            ?: MockDataSource.mockTickets.find { it.id == ticketId }
    }
    var showRefundDialog by rememberSaveable { mutableStateOf(false) }
    var showResaleCancelDialog by rememberSaveable { mutableStateOf(false) }
    var refundErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var resaleCancelErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { AppHeader(title = "티켓 상세", onBack = onBack) },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "상세 정보를 불러오지 못했습니다.\n목록에서 다시 선택해주세요.",
                    color = MutedForeground,
                    fontSize = 14.sp,
                )
            }
            return@Scaffold
        }

        if (showRefundDialog) {
            CheketDialog(
                title = "티켓 환불",
                message = "정말 환불하시겠어요? 환불 후에는 되돌릴 수 없습니다.",
                confirmText = "환불하기",
                dismissText = "취소",
                onConfirm = {
                    showRefundDialog = false
                    refundViewModel.refundTicket(
                        ticketId = ticket.id,
                        onSuccess = { txId -> onRefundSuccess(txId) },
                        onFailure = { message -> refundErrorMessage = message },
                    )
                },
                onDismiss = { showRefundDialog = false },
                isDanger = true,
            )
        }

        if (showResaleCancelDialog) {
            CheketDialog(
                title = "판매 등록 취소",
                message = "판매 등록을 취소하시겠어요? 블록체인 처리 후 다시 보유중 상태로 돌아옵니다.",
                confirmText = "취소 요청하기",
                dismissText = "닫기",
                onConfirm = {
                    showResaleCancelDialog = false
                    resaleCancelViewModel.cancelResale(
                        ticketId = ticket.id,
                        onSuccess = onResaleCancelRequested,
                        onFailure = { message -> resaleCancelErrorMessage = message },
                    )
                },
                onDismiss = { showResaleCancelDialog = false },
                isDanger = true,
            )
        }

        refundErrorMessage?.let { message ->
            CheketAlertDialog(
                title = "환불 실패",
                message = message,
                confirmText = "확인",
                onConfirm = { refundErrorMessage = null },
            )
        }

        resaleCancelErrorMessage?.let { message ->
            CheketAlertDialog(
                title = "판매 등록 취소 실패",
                message = message,
                confirmText = "확인",
                onConfirm = { resaleCancelErrorMessage = null },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = ticket.poster,
                    contentDescription = ticket.showName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                ) {
                    TicketStatusBadge(status = ticket.status)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = ticket.showName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    HorizontalDivider(color = BorderColor)

                    InfoRow(label = "공연일시", value = com.ssafy.cheket.core.util.DateTimeUtils.formatShowDateTime(ticket.showDate))
                    InfoRow(label = "장소", value = ticket.venue)
                    InfoRow(label = "좌석", value = ticket.seatLabel)
                    InfoRow(label = "등급", value = ticket.grade)
                    InfoRow(label = "결제 금액", value = formatPrice(ticket.originalPrice))

                    if (ticket.status == TicketStatus.LISTED && ticket.resalePrice != null) {
                        HorizontalDivider(color = BorderColor)
                        InfoRow(
                            label = "재판매 금액",
                            value = formatPrice(ticket.resalePrice),
                            valueColor = Primary,
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NFT 정보",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    NftInfoRow(
                        label = "Token ID",
                        value = ticket.numbering.ifBlank { ticket.id },
                    )

                    ticket.metadataIpfsCid?.let {
                        NftInfoRow(
                            label = "Metadata CID",
                            value = it,
                        )
                    } ?: Text(
                        text = "메타데이터 연동 전입니다.",
                        fontSize = 12.sp,
                        color = MutedForeground,
                    )
                }
            }

            when (ticket.status) {
                TicketStatus.AVAILABLE -> {
                    Button(
                        onClick = { onQrCheckin(ticket.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("QR 코드 보기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onTransfer(ticket.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("양도하기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onResaleCreate(ticket.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("2차 판매하기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = { showRefundDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Warning.copy(alpha = 0.14f),
                            contentColor = Warning,
                        ),
                        enabled = !refundUiState.isRefunding,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (refundUiState.isRefunding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Warning,
                            )
                        } else {
                            Text("환불하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                TicketStatus.LISTED -> {
                    Button(
                        onClick = { showResaleCancelDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger),
                        enabled = !resaleCancelUiState.isCancelling,
                    ) {
                        if (resaleCancelUiState.isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = White,
                            )
                        } else {
                            Text("판매 등록 취소", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                        }
                    }
                }

                TicketStatus.SOLD -> {
                    DisabledActionButton(text = "판매 완료된 티켓입니다")
                }

                TicketStatus.USED -> {
                    DisabledActionButton(text = "사용 완료된 티켓입니다")
                }

                TicketStatus.EXPIRED -> {
                    DisabledActionButton(text = "만료된 티켓입니다")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = OnBackground,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = MutedForeground)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun NftInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 11.sp, color = MutedForeground)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Muted)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun DisabledActionButton(text: String) {
    Button(
        onClick = { },
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

private fun formatPrice(price: Int): String {
    val format = NumberFormat.getNumberInstance(Locale.KOREA)
    return "${format.format(price)} SSF"
}
