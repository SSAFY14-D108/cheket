package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TicketCardItem
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.White

@Composable
fun MyTicketsScreen(
    appContainer: AppContainer,
    onTicketClick: (String) -> Unit = {},
    viewModel: MyTicketsViewModel = viewModel(factory = MyTicketsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                AppHeader(title = "내 티켓")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = TicketFilter.entries.toList()) { filter ->
                        val selected = uiState.selectedFilter == filter
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) White else MutedForeground,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onFilterChange(filter) }
                                .background(if (selected) Primary else Muted)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                }
                HorizontalDivider(color = BorderColor)
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            uiState.errorMessage != null -> {
                EmptyState(
                    title = "티켓을 불러오지 못했습니다",
                    description = uiState.errorMessage ?: "잠시 후 다시 시도해주세요.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            uiState.filteredTickets.isEmpty() -> {
                val (emptyTitle, emptyDesc) = when (uiState.selectedFilter) {
                    TicketFilter.ALL -> "보유한 티켓이 없습니다" to "공연을 예매하면 여기에 티켓이 표시됩니다."
                    TicketFilter.AVAILABLE -> "보유 중인 티켓이 없습니다" to "현재 보유 중인 티켓이 없습니다."
                    TicketFilter.LISTED -> "판매 중인 티켓이 없습니다" to "리세일에 등록한 티켓이 없습니다."
                }
                EmptyState(
                    title = emptyTitle,
                    description = emptyDesc,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = uiState.filteredTickets) { ticket ->
                        TicketCardItem(
                            ticket = ticket,
                            onClick = { onTicketClick(ticket.id) },
                        )
                    }
                }
            }
        }
    }
}
