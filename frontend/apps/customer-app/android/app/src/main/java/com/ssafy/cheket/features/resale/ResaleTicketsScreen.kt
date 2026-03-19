package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

private enum class SortMode(val label: String, val apiValue: String?) {
    LATEST("최신순", null),
    PRICE("가격순", "PRICE"),
}

@Composable
fun ResaleTicketsScreen(
    showId: String,
    onResaleItemClick: (resaleItemId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ResaleTicketsViewModel = viewModel(
        factory = ResaleTicketsViewModel.factory(showId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMode by remember { mutableStateOf(SortMode.LATEST) }

    Scaffold(
        topBar = { AppHeader(title = "2차 거래소", onBack = onBack) },
    ) { innerPadding ->
        when (val state = uiState) {
            is ResaleTicketsUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            is ResaleTicketsUiState.Error -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MutedForeground, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.load() },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("다시 시도")
                        }
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding),
                ) {
                    // Show info card
                    ShowInfoCard(showInfo = state.showInfo, ticketCount = sortedTickets.size)

                    // Sort toggle pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SortMode.entries.forEach { mode ->
                            val selected = sortMode == mode
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) White else MutedForeground,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        sortMode = mode
                                        viewModel.load(sort = mode.apiValue)
                                    }
                                    .background(if (selected) Primary else Muted)
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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(sortedTickets, key = { it.ticketId }) { item ->
                                ResaleTicketCard(
                                    item = item,
                                    onClick = { onResaleItemClick(item.ticketId.toString()) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowInfoCard(
    showInfo: ResaleShowInfo,
    ticketCount: Int,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(BorderColor),
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Show poster
            AsyncImage(
                model = showInfo.posterUrl,
                contentDescription = showInfo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Muted),
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = showInfo.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(6.dp))
                // Venue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = showInfo.venue,
                        fontSize = 12.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Selling count badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "판매 중 ${ticketCount}건",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
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
        if (item.discountRate > 0) {
            item.discountRate.toInt()
        } else if (item.originalPrice > 0 && hasDiscount) {
            (discount * 100) / item.originalPrice
        } else 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(BorderColor),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Seat · Grade
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${item.seatLabel} · ${item.grade}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
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
                        text = "%,d CTK".format(item.resalePrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        lineHeight = 18.sp,
                    )
                    if (hasDiscount) {
                        Text(
                            text = "정가 %,d CTK".format(item.originalPrice),
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
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Icon(
                        Icons.Outlined.LocalOffer,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(
                        text = "${discountPct}% 할인",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                    )
                }
            }
        }
    }
}
