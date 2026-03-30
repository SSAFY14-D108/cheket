package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ssafy.cheket.ui.theme.PrimaryLight

private val V0Background = Background
private val V0ActiveFilterBg = PrimaryLight
private val V0ActiveFilterText = Color(0xFF111111)
private val V0ForegroundText = Color(0xFF111111)
private val FilterChipHeight = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    appContainer: AppContainer,
    onTicketClick: (Ticket) -> Unit = {},
    onCollection: () -> Unit = {},
    viewModel: MyTicketsViewModel = viewModel(factory = MyTicketsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    // 로딩 완료 시 리프레시 인디케이터 숨김
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isRefreshing = false
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = TicketFilter.entries.toList()) { filter ->
                        val selected = uiState.selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.onFilterChange(filter) }
                                .background(if (selected) V0ActiveFilterBg else Color.Transparent)
                                .height(FilterChipHeight)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = filter.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) V0ActiveFilterText else MutedForeground,
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE8B4CB),
                                    Color(0xFFCDB4E8),
                                    Color(0xFFB4E8D4),
                                    Color(0xFFABFEFF),
                                    Color(0xFFFCFCD4),
                                ),
                            )
                        )
                        .clickable(onClick = onCollection)
                        .height(FilterChipHeight)
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "컬렉션",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshTickets()
                },
                modifier = Modifier.fillMaxSize(),
            ) {
            when {
                uiState.isLoading && uiState.filteredTickets.isEmpty() -> {
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
                        itemsIndexed(items = uiState.filteredTickets) { index, ticket ->
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
                            if (index < uiState.filteredTickets.lastIndex) {
                                androidx.compose.material3.HorizontalDivider(
                                    color = com.ssafy.cheket.ui.theme.BorderColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
            } // PullToRefreshBox
        }
    }
}
