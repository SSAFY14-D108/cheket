package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocalOffer
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
    val eventName = event?.name ?: allItems.firstOrNull()?.eventName ?: "리세일 티켓"
    val eventDate = event?.date ?: ""

    var sortMode by remember { mutableStateOf(SortMode.LATEST) }

    val sortedItems = remember(sortMode, allItems) {
        when (sortMode) {
            SortMode.LATEST -> allItems
            SortMode.PRICE -> allItems.sortedBy { it.resalePrice }
        }
    }

    Scaffold(
        topBar = {
            // Custom header with event name + date
            Surface(shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로가기",
                            tint = OnBackground,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eventName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (eventDate.isNotEmpty()) {
                            Text(
                                text = eventDate,
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            // Sort toggle pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SortMode.entries.forEach { mode ->
                    val selected = sortMode == mode
                    Text(
                        text = mode.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
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
                    title = "등록된 리세일 티켓이 없습니다",
                    description = "${eventName}의 리세일 티켓이 없습니다.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
            modifier = Modifier.padding(12.dp),
        ) {
            // Poster thumbnail (80x80 square)
            AsyncImage(
                model = item.poster,
                contentDescription = item.eventName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Muted),
            )

            Spacer(Modifier.width(12.dp))

            // Info section
            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Event name
                Text(
                    text = item.eventName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(2.dp))

                // Seat · Grade
                Text(
                    text = "${item.seatLabel} \u00B7 ${item.grade}",
                    fontSize = 12.sp,
                    color = MutedForeground,
                )

                Spacer(Modifier.height(1.dp))

                // Venue with MapPin icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = item.venue,
                        fontSize = 12.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Resale price + discount
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "%,d CTK".format(item.resalePrice),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )
                    if (hasDiscount && discountPct > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "${discountPct}% 할인",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                            )
                        }
                    }
                }

                // Original price (strikethrough)
                if (hasDiscount) {
                    Text(
                        text = "정가 %,d CTK".format(item.originalPrice),
                        fontSize = 12.sp,
                        color = MutedForeground,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
        }
    }
}
