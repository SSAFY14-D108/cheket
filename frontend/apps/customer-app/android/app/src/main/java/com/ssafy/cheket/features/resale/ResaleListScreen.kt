package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

@Composable
fun ResaleListScreen(
    onEventClick: (eventId: String) -> Unit,
    onBack: () -> Unit,
) {
    val groupedItems = remember { MockDataSource.getResaleGrouped() }

    Scaffold(
        topBar = { AppHeader(title = "2차 거래소", onBack = onBack) },
    ) { innerPadding ->
        if (groupedItems.isEmpty()) {
            EmptyState(
                title = "조건에 맞는 2차 거래 티켓이 없어요",
                description = "검색어나 필터를 바꿔서 다시 확인해 보세요.",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(groupedItems, key = { it.eventId }) { group ->
                    ResaleEventCard(
                        group = group,
                        onClick = { onEventClick(group.eventId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResaleEventCard(
    group: ResaleGroupItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column {
            AsyncImage(
                model = group.poster,
                contentDescription = group.eventName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Muted),
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = group.eventName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = MutedForeground, modifier = Modifier.size(12.dp))
                    Text(
                        text = group.eventDate,
                        fontSize = 10.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Venue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.LocationOn, null, tint = MutedForeground, modifier = Modifier.size(12.dp))
                    Text(
                        text = group.venue,
                        fontSize = 10.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Min price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Outlined.LocalOffer, null, tint = Primary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "%,d CTK~".format(group.minPrice),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                    )
                }
            }
        }
    }
}
