package com.ssafy.cheket.features.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

@Composable
fun ArchiveScreen(
    onTicketClick: (String) -> Unit,
) {
    val usedTickets = remember {
        MockDataSource.mockTickets.filter { it.status == TicketStatus.USED }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "아카이브",
                actions = {
                    if (usedTickets.isNotEmpty()) {
                        Text(
                            "${usedTickets.size}장",
                            fontSize = 13.sp,
                            color = MutedForeground,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (usedTickets.isEmpty()) {
            EmptyState(
                title = "소장된 티켓이 없습니다",
                description = "관람한 공연의 티켓이 여기에 보관됩니다",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding),
            ) {
                Text(
                    "관람 완료된 티켓을 소장하고 추억을 간직하세요.",
                    fontSize = 12.sp,
                    color = MutedForeground,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(usedTickets, key = { it.id }) { ticket ->
                        ArchiveTicketCard(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveTicketCard(
    ticket: Ticket,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            // Poster
            AsyncImage(
                model = ticket.poster,
                contentDescription = ticket.showName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Gradient overlay
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xDD0B0F1A)),
                            startY = 100f,
                        )
                    )
            )

            // Badge
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Primary.copy(alpha = 0.9f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    "소장됨",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                )
            }

            // Info at bottom
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    ticket.showName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
                Text(
                    ticket.showDate,
                    fontSize = 10.sp,
                    color = White.copy(alpha = 0.7f),
                )
                Text(
                    ticket.seatLabel,
                    fontSize = 10.sp,
                    color = White.copy(alpha = 0.7f),
                )
            }
        }
    }
}
