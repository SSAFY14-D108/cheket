package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

@Composable
fun ResaleScreen(
    appContainer: AppContainer,
    onResaleItemClick: (String) -> Unit = {},
    viewModel: ResaleViewModel = viewModel(factory = ResaleViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        if (uiState.groupedItems.isEmpty() && !uiState.isLoading) {
            // Header even when empty
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            ) {
                Text(
                    "2차 거래소",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "리세일 티켓 구매",
                    fontSize = 12.sp,
                    color = MutedForeground,
                )
            }
            EmptyState(
                "등록된 리세일 티켓이 없습니다",
                "아직 리세일 등록된 티켓이 없습니다.",
                Modifier.fillMaxSize(),
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header spanning full width
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column {
                        Text(
                            "2차 거래소",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "리세일 티켓 구매",
                            fontSize = 12.sp,
                            color = MutedForeground,
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Event grid cards
                items(uiState.groupedItems, key = { it.eventId }) { group ->
                    ResaleEventCard(
                        group = group,
                        onClick = { onResaleItemClick(group.eventId) },
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
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column {
            // Poster image
            AsyncImage(
                model = group.poster,
                contentDescription = group.eventName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .background(Muted),
            )

            // Info section
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Event name
                Text(
                    text = group.eventName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )

                // Item count with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "${group.count}개",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedForeground,
                    )
                }
            }
        }
    }
}
