package com.ssafy.cheket.features.shows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.ShowCardItem
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowsScreen(
    appContainer: AppContainer,
    onShowClick: (String) -> Unit = {},
    viewModel: ShowsViewModel = viewModel(factory = ShowsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var isSearchFocused by remember { mutableStateOf(false) }
    var isRegionSheetOpen by remember { mutableStateOf(false) }
    var pendingRegions by remember { mutableStateOf(uiState.selectedRegions) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisibleIndex to totalItemsCount
        }
            .distinctUntilChanged()
            .filter { (lastVisibleIndex, totalItemsCount) ->
                totalItemsCount > 0 && lastVisibleIndex >= totalItemsCount - 3
            }
            .collect {
                viewModel.loadNextPage()
            }
    }

    if (isRegionSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isRegionSheetOpen = false },
            containerColor = Color.White,
            contentWindowInsets = { WindowInsets.navigationBars },
        ) {
            RegionBottomSheet(
                selectedRegions = pendingRegions,
                onToggleRegion = { region ->
                    pendingRegions = if (region == null) {
                        emptyList()
                    } else if (pendingRegions.contains(region)) {
                        pendingRegions - region
                    } else {
                        pendingRegions + region
                    }
                },
                onReset = { pendingRegions = emptyList() },
                onApply = {
                    val currentRegions = uiState.selectedRegions
                    val removedRegions = currentRegions.filterNot { pendingRegions.contains(it) }
                    val addedRegions = pendingRegions.filterNot { currentRegions.contains(it) }

                    if (pendingRegions.isEmpty() && currentRegions.isNotEmpty()) {
                        viewModel.onRegionToggle(null)
                    } else {
                        removedRegions.forEach { viewModel.onRegionToggle(it) }
                        addedRegions.forEach { viewModel.onRegionToggle(it) }
                    }
                    isRegionSheetOpen = false
                },
            )
        }
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppHeader(
                        title = "공연 목록",
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "알림",
                                    tint = Color(0xFF24332F),
                                )
                            }
                        },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 8.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .gradientBorder(RoundedCornerShape(20.dp))
                                    .padding(1.5.dp),
                            ) {
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = viewModel::onSearchChange,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = OnBackground,
                                    ),
                                    cursorBrush = SolidColor(Primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            viewModel.onSearchSubmit()
                                            focusManager.clearFocus()
                                        },
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 38.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFFF7F8F7))
                                        .onFocusChanged { isSearchFocused = it.isFocused },
                                    decorationBox = { innerTextField ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            if (!isSearchFocused) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = MutedForeground,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }

                                            Box(
                                                modifier = Modifier.weight(1f),
                                                contentAlignment = Alignment.CenterStart,
                                            ) {
                                                if (uiState.searchQuery.isEmpty()) {
                                                    Text(
                                                        text = "공연명, 아티스트, 장소 검색",
                                                        fontSize = 14.sp,
                                                        color = MutedForeground,
                                                    )
                                                }
                                                innerTextField()
                                            }

                                            if (uiState.searchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.onSearchChange("")
                                                        viewModel.onSearchSubmit()
                                                    },
                                                    modifier = Modifier.size(20.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "검색어 지우기",
                                                        tint = MutedForeground,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                        }
                                    },
                                )
                            }

                            AnimatedVisibility(
                                visible = isSearchFocused,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut(),
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.onSearchSubmit()
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = Color(0xFF24332F),
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "검색",
                                        tint = Color(0xFF24332F),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SortOption.entries.forEach { sort ->
                                val selected = uiState.sortBy == sort
                                Text(
                                    text = sort.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) Color(0xFF111111) else MutedForeground,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { viewModel.onSortChange(sort) }
                                        .then(
                                            if (selected) {
                                                Modifier
                                                    .shadow(1.dp, RoundedCornerShape(18.dp))
                                                    .background(Color(0xFFE9EFEC))
                                            } else {
                                                Modifier.background(Color.Transparent)
                                            }
                                        )
                                        .padding(vertical = 10.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        RegionSummaryButton(
                            summary = regionSummaryText(uiState.selectedRegions),
                            hasSelection = uiState.selectedRegions.isNotEmpty(),
                            onClick = {
                                pendingRegions = uiState.selectedRegions
                                isRegionSheetOpen = true
                            },
                        )

                        if (viewModel.hasActiveFilters()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { viewModel.resetFilters() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MutedForeground,
                                )
                                Text(
                                    text = "필터 초기화",
                                    fontSize = 12.sp,
                                    color = MutedForeground,
                                )
                            }
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
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "총 ${uiState.totalElements}개의 공연",
                    fontSize = 12.sp,
                    color = MutedForeground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (uiState.shows.isEmpty() && !uiState.isLoading) {
                    EmptyState(
                        title = "공연을 찾을 수 없어요",
                        description = "검색어나 지역 필터를 바꿔서 다시 찾아보세요.",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(uiState.shows, key = { index, show -> "${show.id}_$index" }) { _, show ->
                            ShowCardItem(show = show, onClick = { onShowClick(show.id) })
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = Primary,
                                    )
                                }
                            }
                        }

                        if (
                            uiState.shows.isNotEmpty() &&
                            !uiState.isLoading &&
                            !uiState.isLoadingMore &&
                            !uiState.hasMore
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "더 이상 공연이 없습니다",
                                        fontSize = 12.sp,
                                        color = MutedForeground,
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

@Composable
private fun RegionSummaryButton(
    summary: String,
    hasSelection: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hasSelection) Color(0xFFEEF2F1) else Color(0xFFF7F8F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "지역",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111111),
        )
        Text(
            text = summary,
            fontSize = 12.sp,
            fontWeight = if (hasSelection) FontWeight.Medium else FontWeight.Normal,
            color = MutedForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MutedForeground,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun RegionBottomSheet(
    selectedRegions: List<RegionOption>,
    onToggleRegion: (RegionOption?) -> Unit,
    onReset: () -> Unit,
    onApply: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "지역 선택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "원하는 지역을 선택해서 공연 목록을 좁혀보세요.",
                fontSize = 13.sp,
                color = MutedForeground,
            )
            Spacer(modifier = Modifier.height(20.dp))

            val regionItems = listOf<Pair<String, RegionOption?>>("전체" to null) +
                RegionOption.entries.map { it.label to it }

            regionItems.chunked(6).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowItems.forEach { (label, region) ->
                        RegionSheetChip(
                            label = label,
                            selected = if (region == null) selectedRegions.isEmpty() else selectedRegions.contains(region),
                            onClick = { onToggleRegion(region) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(6 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        HorizontalDivider(color = Color(0xFFF1F3F2))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onReset,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F3F5),
                    contentColor = Color(0xFF4B5563),
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
            ) {
                Text("초기화", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111111),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("적용", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RegionSheetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Primary else Color(0xFFF7F8F7))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else OnBackground,
        )
    }
}

private fun regionSummaryText(selectedRegions: List<RegionOption>): String {
    return when {
        selectedRegions.isEmpty() -> "전체"
        selectedRegions.size == 1 -> selectedRegions.first().label
        else -> "${selectedRegions.first().label} 외 ${selectedRegions.size - 1}"
    }
}
