package com.ssafy.cheket.features.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

private val WISHLISTED_EVENT_IDS = setOf("evt_001", "evt_003", "evt_005")

@Composable
fun WishlistScreen(
    onEventClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val allEvents = remember { MockDataSource.mockEvents }
    var wishlistedIds by remember { mutableStateOf(WISHLISTED_EVENT_IDS) }
    val wishlistedEvents = allEvents.filter { it.id in wishlistedIds }

    Scaffold(
        topBar = {
            AppHeader(
                title = "찜한 콘서트",
                onBack = onBack,
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Danger,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${wishlistedEvents.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (wishlistedEvents.isEmpty()) {
            EmptyState(
                title = "찜한 공연이 없습니다",
                description = "관심 있는 공연을 찜해보세요",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(wishlistedEvents, key = { it.id }) { event ->
                    WishlistItem(
                        event = event,
                        onEventClick = { onEventClick(event.id) },
                        onRemove = { wishlistedIds = wishlistedIds - event.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun WishlistItem(
    event: Event,
    onEventClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = event.poster,
                contentDescription = event.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 70.dp, height = 94.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Muted),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    event.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
                    Text(event.date, fontSize = 12.sp, color = MutedForeground)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
                    Text(event.venue, fontSize = 12.sp, color = MutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val minPrice = event.grades.minOfOrNull { it.price } ?: 0
                Text(
                    "%,d CTK~".format(minPrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                )
                Spacer(Modifier.height(2.dp))
                TextButton(
                    onClick = onRemove,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Danger),
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("찜 해제", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            // ChevronRight per v0-version2
            Icon(Icons.Outlined.ChevronRight, null, tint = SubText, modifier = Modifier.size(20.dp))
        }
    }
}
