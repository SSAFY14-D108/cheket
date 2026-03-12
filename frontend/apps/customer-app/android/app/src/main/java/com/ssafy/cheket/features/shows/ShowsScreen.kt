package com.ssafy.cheket.features.shows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.ShowCardItem
import com.ssafy.cheket.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowsScreen(
    appContainer: AppContainer,
    onShowClick: (String) -> Unit = {},
    viewModel: ShowsViewModel = viewModel(factory = ShowsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 1.dp) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    // ── Search bar + 검색 버튼 (포커스 시에만 표시) ──
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchChange,
                            placeholder = { Text("공연명, 아티스트, 장소 검색", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedForeground) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.onSearchChange("")
                                        viewModel.onSearchSubmit()
                                    }) {
                                        Icon(Icons.Default.Close, "지우기", tint = MutedForeground, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                viewModel.onSearchSubmit()
                                focusManager.clearFocus()
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Surface,
                                unfocusedContainerColor = Surface,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isSearchFocused = it.isFocused },
                        )

                        // 검색 버튼: 포커스 시에만 표시
                        AnimatedVisibility(
                            visible = isSearchFocused,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                        ) {
                            FilledIconButton(
                                onClick = {
                                    viewModel.onSearchSubmit()
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Primary,
                                    contentColor = White,
                                ),
                            ) {
                                Icon(Icons.Default.Search, "검색", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // ── Sort pills ──
                    Row(
                        Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SortOption.entries.forEach { sort ->
                            val sel = uiState.sortBy == sort
                            Text(
                                sort.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (sel) White else MutedForeground,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.onSortChange(sort) }
                                    .then(
                                        if (sel) Modifier
                                            .background(Primary)
                                            .border(1.dp, Primary, RoundedCornerShape(12.dp))
                                        else Modifier
                                            .background(Muted)
                                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    )
                                    .padding(vertical = 8.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                            )
                        }
                    }

                    // ── 지역 필터 (항상 표시) ──
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RegionPill("전체", uiState.selectedRegions.isEmpty()) {
                            viewModel.onRegionToggle(null)
                        }
                        RegionOption.entries.forEach { region ->
                            RegionPill(region.label, uiState.selectedRegions.contains(region)) {
                                viewModel.onRegionToggle(region)
                            }
                        }
                    }

                    // ── 필터 초기화 버튼 ──
                    if (viewModel.hasActiveFilters()) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.resetFilters() }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp), tint = MutedForeground)
                            Text("필터 초기화", fontSize = 12.sp, color = MutedForeground)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            Column(Modifier.fillMaxSize()) {
                // Result count
                Text(
                    "총 ${uiState.totalElements}개의 공연",
                    fontSize = 12.sp,
                    color = MutedForeground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (uiState.shows.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        "공연이 없어요",
                        "검색어나 필터를 바꿔서 다시 확인해 보세요.",
                        Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.shows) { show ->
                            ShowCardItem(show = show, onClick = { onShowClick(show.id) })
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    // Pagination bar
                    if (uiState.totalPages > 1) {
                        PaginationBar(
                            currentPage = uiState.currentPage,
                            totalPages = uiState.totalPages,
                            onPageClick = viewModel::goToPage,
                        )
                    }
                }
            }
        }
    }
}

// ── Pagination Bar ──

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageClick: (Int) -> Unit,
) {
    val windowSize = 5
    val halfWindow = windowSize / 2
    val startPage = (currentPage - halfWindow).coerceIn(0, (totalPages - windowSize).coerceAtLeast(0))
    val endPage = (startPage + windowSize - 1).coerceAtMost(totalPages - 1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous button
        IconButton(
            onClick = { onPageClick(currentPage - 1) },
            enabled = currentPage > 0,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = if (currentPage > 0) OnBackground else MutedForeground.copy(alpha = 0.3f),
            )
        }

        // Page numbers
        for (page in startPage..endPage) {
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(Primary)
                        else Modifier
                    )
                    .clickable { onPageClick(page) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${page + 1}",
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) White else MutedForeground,
                )
            }
        }

        // Next button
        IconButton(
            onClick = { onPageClick(currentPage + 1) },
            enabled = currentPage < totalPages - 1,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "다음",
                tint = if (currentPage < totalPages - 1) OnBackground else MutedForeground.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun RegionPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) White else MutedForeground,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.background(Primary)
                else Modifier.border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}
