package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.ssafy.cheket.ui.theme.*

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
                    items(TicketFilter.entries) { filter ->
                        val sel = uiState.selectedFilter == filter
                        Text(filter.label, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sel) White else MutedForeground,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onFilterChange(filter) }
                                .background(if (sel) Primary else Muted)
                                .padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
                HorizontalDivider(color = BorderColor)
            }
        }
    ) { innerPadding ->
        if (uiState.filteredTickets.isEmpty() && !uiState.isLoading) {
            val (emptyTitle, emptyDesc) = when (uiState.selectedFilter) {
                TicketFilter.ALL -> "보유한 티켓이 없습니다" to "공연을 예매하면 여기에 티켓이 표시됩니다."
                TicketFilter.SOLD -> "보유 중인 티켓이 없습니다" to "현재 보유 중인 티켓이 없습니다."
                TicketFilter.LISTED -> "판매 중인 티켓이 없습니다" to "리세일에 등록한 티켓이 없습니다."
            }
            EmptyState(emptyTitle, emptyDesc,
                Modifier.fillMaxSize().padding(innerPadding))
        } else {
            LazyColumn(Modifier.fillMaxSize().background(Background).padding(innerPadding),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredTickets) { ticket -> TicketCardItem(ticket = ticket, onClick = { onTicketClick(ticket.id) }) }
            }
        }
    }
}
