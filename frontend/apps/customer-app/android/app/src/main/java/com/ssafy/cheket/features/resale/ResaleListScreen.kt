package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.ui.theme.*

private val V0Background = Background
private val V0InputBg = Color(0xFFF3F4F6)           // gray-100
private val V0ActiveFilterBg = PrimaryLight
private val V0ActiveFilterText = Color(0xFF111111)
private val V0PriceText = Color(0xFF333333)
private val V0FilterChipBg = Color(0xFFF3F4F6)

private val REGIONS = listOf("전체", "서울", "경기", "인천", "부산", "대구", "광주", "경남", "전북")

@Composable
fun ResaleListScreen(
    onShowClick: (showId: String) -> Unit,
    onBack: () -> Unit,
) {
    val groupedItems = remember { MockDataSource.getResaleGrouped() }
    var query by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("전체") }

    val filteredItems = remember(groupedItems, query, selectedRegion) {
        val keyword = query.trim().lowercase()
        groupedItems.filter { group ->
            val matchQuery = keyword.isEmpty() ||
                    group.showName.lowercase().contains(keyword) ||
                    group.venue.lowercase().contains(keyword)
            val matchRegion = selectedRegion == "전체" // region filtering placeholder
            matchQuery && matchRegion
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "2차 거래소", onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding)
                
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(V0InputBg)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                "공연명, 아티스트, 장소 검색",
                                fontSize = 14.sp,
                                color = MutedForeground,
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = V0ActiveFilterText,
                            ),
                            cursorBrush = SolidColor(V0ActiveFilterText),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Region filter pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                REGIONS.forEach { region ->
                    val isActive = selectedRegion == region
                    Text(
                        text = region,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) V0ActiveFilterText else MutedForeground,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                selectedRegion = if (region == "전체") "전체"
                                else if (selectedRegion == region) "전체"
                                else region
                            }
                            .background(if (isActive) V0ActiveFilterBg else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            // Active filter chip with X
            if (selectedRegion != "전체") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(V0FilterChipBg)
                            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    ) {
                        Text(
                            text = selectedRegion,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = V0PriceText,
                        )
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "필터 제거",
                            tint = V0PriceText,
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { selectedRegion = "전체" },
                        )
                    }
                }
            }

            // Content
            if (filteredItems.isEmpty()) {
                EmptyState(
                    title = "조건에 맞는 2차 거래 티켓이 없어요",
                    description = "검색어나 필터를 바꿔서 다시 확인해 보세요.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredItems, key = { it.showId }) { group ->
                        ResaleEventCard(
                            group = group,
                            onClick = { onShowClick(group.showId) },
                        )
                    }
                }
            }
        }
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
            .elevatedSurfaceSoft(shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Poster — fills card width, object-cover
        AsyncImage(
            model = group.poster,
            contentDescription = group.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .background(Muted),
        )

        // Info section
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Title — min 2 lines height
            Text(
                text = group.showName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                modifier = Modifier.heightIn(min = 32.dp),
            )

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = group.showDate,
                    fontSize = 10.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Venue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = group.venue,
                    fontSize = 10.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Min price
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Icon(
                    Icons.Outlined.LocalOffer,
                    contentDescription = null,
                    tint = Color(0xFF333333),
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "%,d SSF~".format(group.minPrice),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                )
            }
        }
    }
}
