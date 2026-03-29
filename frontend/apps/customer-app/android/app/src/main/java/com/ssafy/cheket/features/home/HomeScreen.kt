package com.ssafy.cheket.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.zIndex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.R
import com.ssafy.cheket.core.model.BannerSlide
import com.ssafy.cheket.core.model.LikedShow
import com.ssafy.cheket.core.model.OpenScheduleItem
import com.ssafy.cheket.core.model.RankingItem
import com.ssafy.cheket.core.model.ResaleItem
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Black
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryLight
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val V0Background = Background
private val V0Card = Color(0xFFFFFFFF)
private val V0TextPrimary = Color(0xFF111111)
private val V0TextMuted = Color(0xFF667085)
private val V0TextSub = Color(0xFF9CA3AF)
private val V0ActiveFilterBg = PrimaryLight
private val V0SectionDivider = Color(0xFFF3F4F6)
private val V0Gray500 = Color(0xFF6B7280)
private val V0Gray900 = Color(0xFF111827)
private val V0Red500 = Color(0xFFEF4444)
private val V0RankLabel = Color(0xFF7B88C7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appContainer: AppContainer,
    onShowClick: (String) -> Unit = {},
    onMyPage: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onOpenSoon: () -> Unit = {},
    onLikedShowsMore: () -> Unit = {},
    onResaleMore: () -> Unit = {},
    onSeatMapTest: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isRefreshing = false
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.cheket_logo2),
                        contentDescription = "CHEKET",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(32.dp),
                    )
                },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "알림",
                            tint = V0TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = V0Card),
            )
        },
        containerColor = V0Background,
    ) { innerPadding ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding),
        ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                HeroBanner(
                    slides = uiState.bannerSlides,
                    onSlideClick = onShowClick,
                )
            }

            item {
                    Column(Modifier.padding(top = 20.dp, bottom = 16.dp)) {
                        HomeSectionHeader(title = "랭킹")
                        RankingSection(
                            items = uiState.rankingItems,
                            onItemClick = onShowClick,
                        )
                    }
                }

            item {
                    OpenScheduleSection(
                        items = uiState.openSchedule,
                        onItemClick = onShowClick,
                        onMore = onOpenSoon,
                    )
                }

            item {
                    LikedShowsSection(
                        likedShows = uiState.likedShows,
                        onShowClick = onShowClick,
                        onMore = onLikedShowsMore,
                    )
                }

            item {
                    ResaleDiscountSection(
                        resaleItems = uiState.resaleItems,
                        onItemClick = onShowClick,
                        onMore = onResaleMore,
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        } // PullToRefreshBox
    }
}

@Composable
private fun HeroBanner(slides: List<BannerSlide>, onSlideClick: (String) -> Unit) {
    if (slides.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(Color(0xFFE5E7EB)),
        )
        return
    }

    val pagerState = rememberPagerState { slides.size }
    LaunchedEffect(pagerState, slides.size) {
        while (true) {
            delay(3500)
            val pageCount = pagerState.pageCount
            if (pageCount > 1) {
                val safeCurrentPage = pagerState.currentPage.coerceIn(0, pageCount - 1)
                val nextPage = (safeCurrentPage + 1) % pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f),
    ) {
        HorizontalPager(state = pagerState) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onSlideClick(slide.showId) },
            ) {
                AsyncImage(
                    model = slide.image,
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.4f to Color(0x33000000),
                                    1f to Color(0xB3000000),
                                ),
                            ),
                        ),
                )
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        text = slide.title,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp,
                    )
                    Text(
                        text = slide.subtitle,
                        color = White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = slide.venue,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = slide.dates,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        if (slides.size > 1) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${slides.size}",
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(slides.size) { index ->
                    Box(
                        Modifier
                            .size(
                                width = if (index == pagerState.currentPage) 16.dp else 6.dp,
                                height = 6.dp,
                            )
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (index == pagerState.currentPage) White else White.copy(alpha = 0.4f),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(title: String, onMore: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = V0TextPrimary,
        )
        if (onMore != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onMore() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("더보기", fontSize = 12.sp, color = V0TextMuted)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = V0TextMuted,
                )
            }
        }
    }
}

@Composable
private fun RankingSection(items: List<RankingItem>, onItemClick: (String) -> Unit) {
    if (items.isEmpty()) {
        HomeSectionEmptyCard(
            title = "\uC9D1\uACC4 \uC911\uC778 \uB7AD\uD0B9\uC774 \uC5C6\uC5B4\uC694",
            description = "\uC870\uAE08 \uB4A4\uC5D0 \uB2E4\uC2DC \uD655\uC778\uD558\uBA74 \uC778\uAE30 \uACF5\uC5F0\uC744 \uBCF4\uC5EC\uB4DC\uB9B4\uAC8C\uC694.",
        )
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(items.take(5)) { index, item ->
            val rank = index + 1
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                    .clickable { onItemClick(item.showId) }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box {
                    val rankFontSize = if (rank == 1) 52.sp else 46.sp
                    val rankModifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 2.dp, top = 2.dp)
                        .zIndex(2f)
                    Text(
                        text = rank.toString(),
                        fontSize = rankFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color(0xCC101828),
                        textAlign = TextAlign.Center,
                        modifier = rankModifier.offset(x = 1.dp, y = 1.dp),
                    )
                    Text(
                        text = rank.toString(),
                        fontSize = rankFontSize,
                        fontWeight = FontWeight.Black,
                        color = if (rank == 1) Color(0xFFF8FBFF) else Color(0xFFF4F7FF),
                        textAlign = TextAlign.Center,
                        modifier = rankModifier,
                    )
                    AsyncImage(
                        model = item.poster,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(176.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .zIndex(1f),
                    )
                }
                Text(
                    text = item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(18.dp),
                )
                Text(
                    text = item.venue,
                    fontSize = 10.sp,
                    color = V0Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OpenScheduleSection(
    items: List<OpenScheduleItem>,
    onItemClick: (String) -> Unit,
    onMore: () -> Unit = {},
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val cardWidth = minOf((screenWidth * 82 / 100), 320).dp

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "인기 오픈 예정", onMore = onMore)
        if (items.isEmpty()) {
            HomeSectionEmptyCard(
                title = "\uC608\uC815\uB41C \uC624\uD508 \uC77C\uC815\uC774 \uC5C6\uC5B4\uC694",
                description = "\uC0C8\uB85C \uC5F4\uB9B4 \uACF5\uC5F0\uC774 \uC0DD\uAE30\uBA74 \uC5EC\uAE30\uC11C \uBC14\uB85C \uBCFC \uC218 \uC788\uC5B4\uC694.",
            )
            return@Column
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(items) { index, item ->
                Row(
                    modifier = Modifier
                        .width(cardWidth)
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(12.dp),
                ) {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 80.dp, height = 108.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                    ) {
                        if (item.openLabel.isNotBlank()) {
                            Text(
                                text = item.openLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                maxLines = 1,
                            )
                        }

                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Gray900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        if (item.venue.isNotBlank()) {
                            Text(
                                text = item.venue,
                                fontSize = 12.sp,
                                color = V0Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }

                        val dateLabel = buildShowDateLabel(item.showDate, item.showEndDate)
                        if (dateLabel.isNotBlank()) {
                            Text(
                                text = dateLabel,
                                fontSize = 11.sp,
                                color = V0TextSub,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        if (item.tags.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.tags.forEach { tag ->
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = V0TextMuted,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(V0ActiveFilterBg)
                                            .padding(horizontal = 8.dp, vertical = 2.dp),
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

private fun buildShowDateLabel(startDate: String, endDate: String): String {
    if (startDate.isBlank()) return ""
    return try {
        val start = startDate.split("-")
        val startMonth = start[1].toInt()
        val startDay = start[2].toInt()
        if (endDate.isNotBlank() && endDate != startDate) {
            val end = endDate.split("-")
            val endMonth = end[1].toInt()
            val endDay = end[2].toInt()
            "$startMonth/$startDay ~ $endMonth/$endDay"
        } else {
            "$startMonth/$startDay"
        }
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun LikedShowsSection(
    likedShows: List<LikedShow>,
    onShowClick: (String) -> Unit,
    onMore: () -> Unit,
) {
    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "\uCC1C\uD55C \uACF5\uC5F0", onMore = onMore)

        if (likedShows.isEmpty()) {
            HomeSectionEmptyCard(
                title = "\uC544\uC9C1 \uCC1C\uD55C \uACF5\uC5F0\uC774 \uC5C6\uC5B4\uC694",
                description = "\uB9C8\uC74C\uC5D0 \uB4DC\uB294 \uACF5\uC5F0\uC744 \uCC1C\uD558\uBA74 \uC5EC\uAE30\uC11C \uBAA8\uC544\uBCFC \uC218 \uC788\uC5B4\uC694.",
            )
            return@Column
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(likedShows) { show ->
                Column(
                    modifier = Modifier
                        .width(128.dp)
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onShowClick(show.showId) }
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box {
                        AsyncImage(
                            model = show.posterUrl,
                            contentDescription = show.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                        )
                        Box(
                            Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Text(
                        text = show.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                        modifier = Modifier.heightIn(min = 34.dp),
                    )
                    Text(
                        text = show.venue,
                        fontSize = 10.sp,
                        color = V0Gray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResaleDiscountSection(
    resaleItems: List<ResaleItem>,
    onItemClick: (String) -> Unit,
    onMore: () -> Unit,
) {
    val discounted = remember(resaleItems) {
        resaleItems.mapNotNull { item ->
            if (item.originalPrice <= 0) return@mapNotNull null
            val percent =
                ((item.originalPrice - item.resalePrice).toFloat() / item.originalPrice * 100).roundToInt()
            if (percent > 0) item to percent else null
        }.sortedByDescending { it.second }.take(5)
    }
    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "\uD56B\uD55C \uB9AC\uC138\uC77C", onMore = onMore)
        if (discounted.isEmpty()) {
            HomeSectionEmptyCard(
                title = "\uC9C0\uAE08\uC740 \uB458\uB7EC\uBCFC \u0032\uCC28 \uAC70\uB798\uAC00 \uC5C6\uC5B4\uC694",
                description = "\uC0C8\uB85C\uC6B4 \uAC70\uB798\uAC00 \uC62C\uB77C\uC624\uBA74 \uC774 \uC139\uC158\uC5D0\uC11C \uBC14\uB85C \uD655\uC778\uD560 \uC218 \uC788\uC5B4\uC694.",
            )
            return@Column
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            discounted.forEach { (item, percent) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(16.dp),
                ) {
                    Box {
                        AsyncImage(
                            model = item.poster,
                            contentDescription = item.showName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 96.dp, height = 128.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                        )
                        Box(
                            Modifier
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(V0Red500)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .align(Alignment.TopStart),
                        ) {
                            Text(text = "-$percent%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = V0TextMuted,
                            )
                            Text(
                                text = "2차 거래소",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = V0TextMuted,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = item.showName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Gray900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                        )
                        Text(
                            text = item.venue.split(",").first().trim(),
                            fontSize = 12.sp,
                            color = V0Gray500,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            text = "${item.seatLabel} 쨌 ${item.grade}",
                            fontSize = 12.sp,
                            color = V0Gray500,
                        )
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "%,d SSF".format(item.originalPrice),
                                fontSize = 12.sp,
                                color = V0TextSub,
                                textDecoration = TextDecoration.LineThrough,
                            )
                            Text(
                                text = "%,d SSF".format(item.resalePrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = V0Red500,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionEmptyCard(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Gray900,
        )
        Text(
            text = description,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = V0TextMuted,
        )
    }
}


