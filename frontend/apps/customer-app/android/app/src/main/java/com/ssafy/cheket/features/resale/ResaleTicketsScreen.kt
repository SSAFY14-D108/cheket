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
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.ResaleItem
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

private enum class SortMode(val label: String) {
    LATEST("최신순"),
    PRICE("가격순"),
}

@Composable
fun ResaleTicketsScreen(
    eventId: String,
    onResaleItemClick: (resaleItemId: String) -> Unit,
    onBack: () -> Unit,
) {
    val allItems = remember { MockDataSource.mockResaleItems.filter { it.eventId == eventId } }
    val event = remember { MockDataSource.mockEvents.find { it.id == eventId } }

    var sortMode by remember { mutableStateOf(SortMode.LATEST) }

    val sortedItems = remember(sortMode, allItems) {
        when (sortMode) {
            SortMode.LATEST -> allItems
            SortMode.PRICE -> allItems.sortedBy { it.resalePrice }
        }
    }

    if (event == null) {
        Scaffold(topBar = { AppHeader(title = "2차 거래소", onBack = onBack) }) { innerPadding ->
            EmptyState(
                title = "공연 정보를 찾을 수 없습니다.",
                description = "",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
        return
    }

    Scaffold(
        topBar = { AppHeader(title = "2차 거래소", onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            // Event info card
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
                    // Event poster
                    AsyncImage(
                        model = event.poster,
                        contentDescription = event.name,
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
                            text = event.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        // Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = event.date,
                                fontSize = 12.sp,
                                color = MutedForeground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
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
                                text = event.venue,
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
                                text = "판매 중 ${sortedItems.size}건",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                            )
                        }
                    }
                }
            }

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
                            .clickable { sortMode = mode }
                            .background(if (selected) Primary else Muted)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            if (sortedItems.isEmpty()) {
                EmptyState(
                    title = "등록된 재판매 티켓이 없습니다.",
                    description = "${event.name} 재판매 티켓이 등록되면 여기에서 확인할 수 있습니다.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(sortedItems, key = { it.id }) { item ->
                        ResaleTicketCard(
                            item = item,
                            onClick = { onResaleItemClick(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResaleTicketCard(
    item: ResaleItem,
    onClick: () -> Unit,
) {
    val discount = item.originalPrice - item.resalePrice
    val hasDiscount = discount > 0
    val discountPct = remember(item) {
        if (item.originalPrice > 0 && hasDiscount) {
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
                // Seat · Grade (primary info with ticket icon)
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

                // Event date
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
                        text = item.eventDate,
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
