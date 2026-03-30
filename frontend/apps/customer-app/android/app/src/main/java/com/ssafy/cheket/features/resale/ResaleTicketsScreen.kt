package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.CheketAlertDialog
import com.ssafy.cheket.core.ui.component.CheketDialog
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*

private val V0Background = Background
private val V0ActiveFilterBg = PrimaryLight
private val V0ActiveFilterText = Color(0xFF111111)
private val V0IconTint = Color(0xFF333333)
private val V0SellingBadgeBg = Color(0xFFF3F4F6)
private val V0DiscountBadgeBg = Color(0xFFF3F4F6)

private enum class SortMode(val label: String, val apiValue: String?) {
    LATEST("최신순", null),
    PRICE("가격순", "PRICE"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResaleTicketsScreen(
    showId: String,
    onResaleItemClick: (resaleItemId: String) -> Unit,
    onTxProcessing: (txId: Long, txType: String) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    viewModel: ResaleTicketsViewModel = viewModel(
        factory = ResaleTicketsViewModel.factory(showId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMode by remember { mutableStateOf(SortMode.LATEST) }

    // 상세 BottomSheet 상태
    var selectedTicket by remember { mutableStateOf<ResaleTicketUiItem?>(null) }
    var showPurchaseConfirm by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }
    var purchaseErrorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "2차 거래소",
                onBack = onBack,
                helpTutorialId = TutorialId.RESALE_DETAIL,
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is ResaleTicketsUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(V0Background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MutedForeground,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            is ResaleTicketsUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(V0Background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MutedForeground, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "다시 시도",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0ActiveFilterText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(V0ActiveFilterBg)
                                .clickable { viewModel.load() }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            is ResaleTicketsUiState.Success -> {
                val sortedTickets = remember(sortMode, state.tickets) {
                    when (sortMode) {
                        SortMode.LATEST -> state.tickets
                        SortMode.PRICE -> state.tickets.sortedBy { it.resalePrice }
                    }
                }
                var isRefreshing by remember { mutableStateOf(false) }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.load(sort = sortMode.apiValue)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(V0Background)
                        .padding(innerPadding),
                ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Show info card
                    ShowInfoCard(showInfo = state.showInfo, ticketCount = sortedTickets.size)

                    // Sort pills + ticket list
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
                        // Sort toggle pills
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SortMode.entries.forEach { mode ->
                                val selected = sortMode == mode
                                Text(
                                    text = mode.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) V0ActiveFilterText else MutedForeground,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            sortMode = mode
                                            viewModel.load(sort = mode.apiValue)
                                        }
                                        .background(if (selected) V0ActiveFilterBg else Color.Transparent)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }

                        if (sortedTickets.isEmpty()) {
                            EmptyState(
                                title = "등록된 재판매 티켓이 없습니다.",
                                description = "${state.showInfo.title} 재판매 티켓이 등록되면 여기에서 확인할 수 있습니다.",
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                            ) {
                                items(sortedTickets, key = { it.ticketId }) { item ->
                                    ResaleTicketCard(
                                        item = item,
                                        onClick = { selectedTicket = item },
                                    )
                                }
                            }
                        }
                    }
                }
                } // PullToRefreshBox
            }
        }
    }

    // ── 티켓 상세 BottomSheet ──
    selectedTicket?.let { ticket ->
        ModalBottomSheet(
            onDismissRequest = { selectedTicket = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "2차 거래 티켓 상세",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0ActiveFilterText,
                )

                // 좌석 정보
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResaleDetailRow("좌석", ticket.seatLabel)
                    ResaleDetailRow("등급", ticket.grade)
                    ResaleDetailRow("공연 일시", ticket.showDate)
                    HorizontalDivider(color = Color(0xFFE5E7EB))
                    ResaleDetailRow("정가", "%,d SSF".format(ticket.originalPrice))
                    ResaleDetailRow(
                        "리세일가",
                        "%,d SSF".format(ticket.resalePrice),
                        valueColor = Primary,
                    )
                    if (ticket.originalPrice > ticket.resalePrice) {
                        val savePct = (((ticket.originalPrice - ticket.resalePrice).toLong() * 100L) / ticket.originalPrice.toLong()).toInt()
                        ResaleDetailRow(
                            "할인",
                            "${savePct}% 할인 (${ticket.originalPrice - ticket.resalePrice} SSF 절약)",
                            valueColor = Danger,
                        )
                    }
                }

                // 구매 버튼
                Button(
                    onClick = { showPurchaseConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = !isPurchasing,
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            "구매하기 · %,d SSF".format(ticket.resalePrice),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    // ── 구매 확인 다이얼로그 ──
    if (showPurchaseConfirm && selectedTicket != null) {
        CheketDialog(
            title = "2차 거래 티켓 구매",
            message = "${selectedTicket!!.seatLabel} (${selectedTicket!!.grade})\n" +
                    "가격: %,d SSF\n\n".format(selectedTicket!!.resalePrice) +
                    "구매하시겠습니까? 블록체인에 기록되며 취소할 수 없습니다.",
            confirmText = "구매",
            dismissText = "취소",
            onConfirm = {
                showPurchaseConfirm = false
                isPurchasing = true
                viewModel.purchaseResale(
                    ticketId = selectedTicket!!.ticketId,
                    onSuccess = { txId ->
                        isPurchasing = false
                        selectedTicket = null
                        onTxProcessing(txId, "RESALE_PURCHASE")
                    },
                    onError = { msg ->
                        isPurchasing = false
                        purchaseErrorMsg = msg
                    },
                )
            },
            onDismiss = { showPurchaseConfirm = false },
        )
    }

    // 구매 에러 다이얼로그
    purchaseErrorMsg?.let { msg ->
        CheketAlertDialog(
            title = "구매 실패",
            message = msg,
            confirmText = "확인",
            onConfirm = { purchaseErrorMsg = null },
        )
    }
}

@Composable
private fun ResaleDetailRow(
    label: String,
    value: String,
    valueColor: Color = V0ActiveFilterText,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = MutedForeground)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun ShowInfoCard(
    showInfo: ResaleShowInfo,
    ticketCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .elevatedSurfaceSoft(shape = RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Show poster — 80x80 rounded-xl
            AsyncImage(
                model = showInfo.posterUrl,
                contentDescription = showInfo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Muted),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Title
                Text(
                    text = showInfo.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0ActiveFilterText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(6.dp))

                // Date
                if (showInfo.showDate.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = V0IconTint,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = showInfo.showDate,
                            fontSize = 12.sp,
                            color = MutedForeground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp,
                        )
                    }
                }

                // Venue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = V0IconTint,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = showInfo.venue,
                        fontSize = 12.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Selling count badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(V0SellingBadgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        tint = V0IconTint,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "판매 중 ${ticketCount}건",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0IconTint,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResaleTicketCard(
    item: ResaleTicketUiItem,
    onClick: () -> Unit,
) {
    val discount = item.originalPrice - item.resalePrice
    val hasDiscount = discount > 0
    val discountPct = remember(item) {
        if (item.originalPrice > 0 && hasDiscount) {
            ((discount.toLong() * 100L) / item.originalPrice.toLong()).toInt()
        } else 0
    }

    // 할인률에 따른 카드 스타일
    val borderMod = when {
        discountPct >= 40 -> Modifier
            .gradientBorder(shape = RoundedCornerShape(12.dp), borderWidth = 1.5.dp)
        discountPct >= 20 -> Modifier
            .border(1.dp, Primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        else -> Modifier
            .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(borderMod)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            // Seat + Grade
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Outlined.ConfirmationNumber,
                    contentDescription = null,
                    tint = V0IconTint,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "${item.seatLabel} · ${item.grade}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0ActiveFilterText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(2.dp))

            // Show date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = item.showDate,
                    fontSize = 11.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Price row
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "%,d SSF".format(item.resalePrice),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0ActiveFilterText,
                    lineHeight = 18.sp,
                )
                if (hasDiscount) {
                    Text(
                        text = "정가 %,d SSF".format(item.originalPrice),
                        fontSize = 11.sp,
                        color = MutedForeground,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
        }

        // Discount badge pill
        if (hasDiscount && discountPct > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(start = 8.dp, top = 2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(V0DiscountBadgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Icon(
                    Icons.Outlined.LocalOffer,
                    contentDescription = null,
                    tint = V0IconTint,
                    modifier = Modifier.size(10.dp),
                )
                Text(
                    text = "${discountPct}% 할인",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0IconTint,
                )
            }
        }
    }
}
