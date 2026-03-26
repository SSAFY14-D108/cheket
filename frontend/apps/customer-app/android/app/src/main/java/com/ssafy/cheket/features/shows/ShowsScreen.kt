package com.ssafy.cheket.features.shows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
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
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val FilterBarBackground = Color(0xFFF4F6F5)
private val FilterChipBackground = Color(0xFFFFFFFF)
private val FilterChipBorder = Color(0xFFE5E7EB)
private val FilterSelectedText = Color(0xFF111827)
private val FilterSelectedChip = Color(0xFFF3F4F6)
private val RegionChipSelected = Primary
private val RegionChipSelectedText = Color(0xFFFFFFFF)
private val RegionChipDefault = Color(0xFFF7F7F6)

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
    var isRegionSheetOpen by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }
    var pendingRegions by remember { mutableStateOf(uiState.selectedRegions) }
    var isSearchMode by remember { mutableStateOf(false) }

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
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = Background,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    AppHeader(
                        title = "공연",
                        actions = {
                            IconButton(
                                onClick = {
                                    if (isSearchMode) {
                                        viewModel.onSearchChange("")
                                        viewModel.onSearchSubmit()
                                        focusManager.clearFocus()
                                    }
                                    isSearchMode = !isSearchMode
                                },
                            ) {
                                Icon(
                                    imageVector = if (isSearchMode) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = if (isSearchMode) "검색 닫기" else "검색",
                                    tint = Color(0xFF24332F),
                                )
                            }
                        },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    )
                    {
                        if (isSearchMode) {
                            SearchField(
                                query = uiState.searchQuery,
                                onValueChange = viewModel::onSearchChange,
                                autoFocus = true,
                                onSubmit = {
                                    viewModel.onSearchSubmit()
                                    focusManager.clearFocus()
                                },
                                onClear = {
                                    viewModel.onSearchChange("")
                                    viewModel.onSearchSubmit()
                                },
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SortDropdownButton(
                                selectedSort = uiState.sortBy,
                                expanded = isSortMenuOpen,
                                onExpandedChange = { isSortMenuOpen = it },
                                onSortChange = {
                                    viewModel.onSortChange(it)
                                    isSortMenuOpen = false
                                },
                            )
                            RegionSummaryButton(
                                summary = regionSummaryText(uiState.selectedRegions),
                                hasSelection = uiState.selectedRegions.isNotEmpty(),
                                onClick = {
                                    pendingRegions = uiState.selectedRegions
                                    isRegionSheetOpen = true
                                },
                            )

                            if (viewModel.hasActiveFilters()) {
                                TextButton(onClick = viewModel::resetFilters) {
                                    Text(
                                        text = "필터 초기화",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MutedForeground,
                                    )
                                }
                            }
                        }

                        FilterSummaryRow(
                            totalCount = uiState.totalElements,
                            selectedRegions = uiState.selectedRegions,
                            selectedSort = uiState.sortBy.label,
                        )
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
                .padding(innerPadding)
                ,
        ) {
            if (uiState.shows.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    title = "공연을 찾을 수 없어요",
                    description = "검색어나 지역 필터를 바꿔서 다시 찾아보세요.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
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
                                    .padding(top = 8.dp),
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

@Composable
private fun SearchField(
    query: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
        }
    }
    val searchBorderBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFD9EFE9),
                Color(0xFFE4E7FB),
            ),
        )
    }

    BasicTextField(
        value = query,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = OnBackground,
        ),
        cursorBrush = SolidColor(Primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(BorderStroke(1.dp, searchBorderBrush), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .then(if (autoFocus) Modifier.focusRequester(focusRequester) else Modifier),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(18.dp),
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "공연명, 아티스트, 장소 검색",
                            fontSize = 14.sp,
                            color = MutedForeground,
                        )
                    }
                    innerTextField()
                }

                // X 버튼 제거됨
            }
        },
    )
}

@Composable
private fun SortDropdownButton(
    selectedSort: SortOption,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSortChange: (SortOption) -> Unit,
) {
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(FilterSelectedChip)
                .border(1.dp, FilterChipBorder, RoundedCornerShape(20.dp))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = selectedSort.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = FilterSelectedText,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MutedForeground,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color.White),
        ) {
            SortOption.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = sort.label,
                            fontSize = 13.sp,
                            fontWeight = if (sort == selectedSort) FontWeight.SemiBold else FontWeight.Normal,
                            color = OnBackground,
                        )
                    },
                    onClick = { onSortChange(sort) },
                )
            }
        }
    }
}

@Composable
private fun RegionSummaryButton(
    summary: String,
    hasSelection: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(20.dp))
            .background(FilterChipBackground)
            .border(1.dp, FilterChipBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "지역",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = FilterSelectedText,
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
private fun FilterSummaryRow(
    totalCount: Int,
    selectedRegions: List<RegionOption>,
    selectedSort: String,
) {
    val regionText = if (selectedRegions.isEmpty()) "전체 지역" else regionSummaryText(selectedRegions)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$regionText · $selectedSort",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF35584F),
        )
        Text(
            text = "총 ${totalCount}개",
            fontSize = 12.sp,
            color = MutedForeground,
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
                    containerColor = Primary,
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
            .background(if (selected) RegionChipSelected else RegionChipDefault)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) RegionChipSelectedText else OnBackground,
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
