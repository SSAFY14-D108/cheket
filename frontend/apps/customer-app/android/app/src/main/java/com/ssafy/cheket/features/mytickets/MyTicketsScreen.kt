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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TicketCardItem
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.MutedForeground

private val V0Background = Background
private val V0ActiveFilterBg = Color(0xFFE8EEF9)
private val V0ActiveFilterText = Color(0xFF111111)
private val V0ForegroundText = Color(0xFF111111)

@Composable
fun MyTicketsScreen(
    appContainer: AppContainer,
    onTicketClick: (Ticket) -> Unit = {},
    onCollection: () -> Unit = {},
    viewModel: MyTicketsViewModel = viewModel(factory = MyTicketsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTickets()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            AppHeader(title = "내 티켓")
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = TicketFilter.entries.toList()) { filter ->
                        val selected = uiState.selectedFilter == filter
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selected) V0ActiveFilterText else MutedForeground,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.onFilterChange(filter) }
                                .background(if (selected) V0ActiveFilterBg else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }

                Text(
                    text = "컬렉션",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0ForegroundText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .gradientBorder(shape = RoundedCornerShape(50))
                        .clickable(onClick = onCollection)
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MutedForeground,
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    EmptyState(
                        title = "티켓을 불러오지 못했어요",
                        description = uiState.errorMessage ?: "잠시 후 다시 시도해주세요.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                uiState.filteredTickets.isEmpty() -> {
                    val (emptyTitle, emptyDesc) = when (uiState.selectedFilter) {
                        TicketFilter.ALL -> "보유 중인 티켓이 없어요" to "예매한 티켓이 생기면 이곳에서 확인할 수 있어요."
                        TicketFilter.AVAILABLE -> "사용 가능한 티켓이 없어요" to "현재 입장 가능한 티켓이 없습니다."
                        TicketFilter.LISTED -> "판매 중인 티켓이 없어요" to "현재 리세일 등록된 티켓이 없습니다."
                    }
                    EmptyState(
                        title = emptyTitle,
                        description = emptyDesc,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items = uiState.filteredTickets) { ticket ->
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
