package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import kotlinx.coroutines.launch

private val SearchBackground = Color(0xFFF3F4F6)
private val ActiveChipBackground = Color(0xFFEEF1F0)
private val PriceTextColor = Color(0xFF2B2B2B)
private val ResetButtonBackground = Color(0xFFF1F3F5)
private val RegionButtonBackground = Color(0xFFF4F5F6)
private val RegionSheetButtonBackground = Color(0xFFF7F7F7)
private val RegionSheetHandleColor = Color(0xFF6D8780)
private val HeaderIconTint = Color(0xFF24332F)

private val ResaleRegions = listOf(
    "전체",
    "서울",
    "경기",
    "인천",
    "부산",
    "대구",
    "광주",
    "대전",
    "울산",
    "세종",
    "강원",
    "충북",
    "충남",
    "전북",
    "전남",
    "경북",
    "경남",
    "제주",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResaleScreen(
    appContainer: AppContainer,
    onResaleItemClick: (String) -> Unit = {},
    viewModel: ResaleViewModel = viewModel(factory = ResaleViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var isRegionSheetOpen by rememberSaveable { mutableStateOf(false) }
    val appliedRegions = remember { mutableStateListOf("전체") }
    val pendingRegions = remember { mutableStateListOf("전체") }
    val regionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRegionSheetOpen) {
        if (isRegionSheetOpen) {
            pendingRegions.clear()
            pendingRegions.addAll(appliedRegions)
        }
    }

    val filteredItems = remember(uiState.groupedItems, query, appliedRegions.toList()) {
        val keyword = query.trim().lowercase()
        val selectedRegions = appliedRegions.filterNot { it == "전체" }.toSet()

        uiState.groupedItems.filter { group ->
            val matchesQuery = keyword.isEmpty() ||
                group.showName.lowercase().contains(keyword) ||
                group.venue.lowercase().contains(keyword)

            val regionLabel = normalizeRegion(group.region, group.venue)
            val matchesRegion = selectedRegions.isEmpty() || regionLabel in selectedRegions

            matchesQuery && matchesRegion
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "2차 거래소",
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TutorialHelpButton(
                            tutorialId = TutorialId.RESALE_LIST,
                            tint = HeaderIconTint,
                        )
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "알림",
                                tint = HeaderIconTint,
                            )
                        }
                    }
                },
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            ResaleSearchBar(
                query = query,
                onQueryChange = { query = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
            RegionSummaryButton(
                summary = regionSummary(appliedRegions),
                onClick = { isRegionSheetOpen = true },
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MutedForeground,
                            strokeWidth = 2.dp,
                        )
                    }
                }

                filteredItems.isEmpty() -> {
                    EmptyState(
                        title = "거래 가능한 공연이 없어요",
                        description = "검색어나 지역 필터를 바꿔서 다시 찾아보세요.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(filteredItems, key = { it.showId }) { group ->
                            ResaleEventCard(
                                group = group,
                                onClick = { onResaleItemClick(group.showId) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (isRegionSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isRegionSheetOpen = false },
            sheetState = regionSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(RegionSheetHandleColor),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 18.dp),
            ) {
                Text(
                    text = "지역 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "원하는 지역을 선택해서 리세일 공연을 좁혀보세요.",
                    fontSize = 14.sp,
                    color = MutedForeground,
                )
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResaleRegions.chunked(4).forEach { rowRegions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowRegions.forEach { region ->
                                val selected = pendingRegions.contains(region)
                                RegionChip(
                                    region = region,
                                    selected = selected,
                                    modifier = Modifier.weight(1f),
                                    onClick = { toggleRegionSelection(region, pendingRegions) },
                                )
                            }
                            repeat(4 - rowRegions.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            pendingRegions.clear()
                            pendingRegions.add("전체")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ResetButtonBackground,
                            contentColor = OnBackground,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("초기화", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            appliedRegions.clear()
                            appliedRegions.addAll(pendingRegions)
                            scope.launch { regionSheetState.hide() }.invokeOnCompletion {
                                isRegionSheetOpen = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF141414),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("적용", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResaleSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SearchBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MutedForeground,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "공연명, 아티스트, 장소 검색",
                    fontSize = 16.sp,
                    color = MutedForeground,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = OnBackground,
                ),
                cursorBrush = SolidColor(OnBackground),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RegionSummaryButton(
    summary: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(RegionButtonBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "지역",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackground,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = summary,
            fontSize = 14.sp,
            color = MutedForeground,
        )
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = MutedForeground,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun RegionChip(
    region: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) ActiveChipBackground else RegionSheetButtonBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = region,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = OnBackground,
            maxLines = 1,
        )
    }
}

@Composable
private fun ResaleEventCard(
    group: ResaleGroupItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurfaceSoft(shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = group.poster,
            contentDescription = group.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
                .background(Muted),
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = group.showName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp,
                modifier = Modifier.heightIn(min = 48.dp),
            )

            InfoLine(
                icon = Icons.Outlined.CalendarMonth,
                text = formatResaleDate(group.showDate),
            )
            InfoLine(
                icon = Icons.Outlined.LocationOn,
                text = group.venue,
            )
            InfoLine(
                icon = Icons.Outlined.LocalOffer,
                text = if (group.minPrice > 0) "%,d CTK ~".format(group.minPrice) else "가격 확인",
                tint = PriceTextColor,
                textColor = PriceTextColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun InfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color = MutedForeground,
    textColor: Color = MutedForeground,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = textColor,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun toggleRegionSelection(region: String, selectedRegions: MutableList<String>) {
    if (region == "전체") {
        selectedRegions.clear()
        selectedRegions.add("전체")
        return
    }

    selectedRegions.remove("전체")

    if (selectedRegions.contains(region)) {
        selectedRegions.remove(region)
    } else {
        selectedRegions.add(region)
    }

    if (selectedRegions.isEmpty()) {
        selectedRegions.add("전체")
    }
}

private fun regionSummary(selectedRegions: List<String>): String {
    val regions = selectedRegions.filterNot { it == "전체" }
    return when {
        regions.isEmpty() -> "전체"
        regions.size == 1 -> regions.first()
        else -> "${regions.first()} 외 ${regions.size - 1}"
    }
}

private fun normalizeRegion(region: String, venue: String): String {
    if (region.isNotBlank()) return region

    return ResaleRegions.firstOrNull { candidate ->
        candidate != "전체" && venue.contains(candidate)
    } ?: "전체"
}

private fun formatResaleDate(rawDate: String): String {
    return rawDate.substringBefore('T').replace("-", ".")
}
