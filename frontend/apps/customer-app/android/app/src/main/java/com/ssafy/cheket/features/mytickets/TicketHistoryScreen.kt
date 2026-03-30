package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TicketCardItem
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.PrimaryLight

private val HistoryBackground = Background
private val HistoryActiveBg = PrimaryLight
private val HistoryActiveText = Color(0xFF111111)
private val HistoryInactiveText = Color(0xFF7B8B86)

@Composable
fun TicketHistoryScreen(
    onBack: () -> Unit,
    onTicketClick: (Ticket) -> Unit,
    viewModel: TicketHistoryViewModel = viewModel(factory = TicketHistoryViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppHeader(
                title = "티켓 내역",
                onBack = onBack,
            )
        },
        containerColor = HistoryBackground,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HistoryBackground)
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TicketHistoryTab.entries.forEach { tab ->
                    val selected = uiState.selectedTab == tab
                    val count = if (tab == TicketHistoryTab.UPCOMING) {
                        uiState.upcomingTickets.size
                    } else {
                        uiState.usedTickets.size
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) HistoryActiveBg else Color.Transparent)
                            .clickable { viewModel.selectTab(tab) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) HistoryActiveText else HistoryInactiveText,
                        )
                        Text(
                            text = count.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) HistoryActiveText else HistoryInactiveText,
                        )
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = HistoryInactiveText,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                uiState.visibleTickets.isEmpty() && uiState.visibleErrorMessage != null -> {
                    EmptyState(
                        title = "티켓 내역을 불러오지 못했어요",
                        description = uiState.visibleErrorMessage ?: "잠시 후 다시 시도해 주세요.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                uiState.visibleTickets.isEmpty() -> {
                    val title = if (uiState.selectedTab == TicketHistoryTab.UPCOMING) {
                        "예정된 티켓이 없어요"
                    } else {
                        "사용완료된 티켓이 없어요"
                    }
                    val description = if (uiState.selectedTab == TicketHistoryTab.UPCOMING) {
                        "예매한 티켓은 공연 전까지 여기에서 바로 확인할 수 있어요."
                    } else {
                        "관람이 끝난 티켓은 컬렉션과 함께 이곳에서 확인할 수 있어요."
                    }
                    EmptyState(
                        title = title,
                        description = description,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.visibleTickets) { ticket ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .elevatedSurfaceSoft(shape = RoundedCornerShape(18.dp)),
                                ) {
                                    TicketCardItem(
                                        ticket = ticket,
                                        onClick = { onTicketClick(ticket) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
