package com.ssafy.cheket.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    appContainer: AppContainer,
    onShowClick: (String) -> Unit = {},
    onMyPage: () -> Unit = {},
    onSeatMapTest: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "CHEKET",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Primary,
                        letterSpacing = (-1).sp,
                    )
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "알림", tint = OnBackground)
                        }
                        IconButton(onClick = onMyPage) {
                            Icon(Icons.Filled.Person, contentDescription = "마이페이지", tint = OnBackground)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            // 1. Hero Banner
            item {
                HeroBanner(
                    slides = uiState.bannerSlides,
                    onSlideClick = { onShowClick(it) },
                )
            }

            // thin divider
            item { Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor)) }

            // 2. Ranking
            item {
                Column(Modifier.padding(top = 20.dp, bottom = 16.dp)) {
                    HomeSectionHeader(title = "랭킹", onMore = {})
                    RankingSection(
                        items = uiState.rankingItems,
                        onItemClick = { onShowClick(it) },
                    )
                }
            }

            // section divider
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(Muted.copy(alpha = 0.6f))) }

            // 3. Open Schedule (horizontal cards)
            item {
                OpenScheduleSection(
                    items = uiState.openSchedule,
                    onItemClick = { onShowClick(it) },
                )
            }

            // section divider
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(Muted.copy(alpha = 0.6f))) }

            // 4. Recommendation
            item {
                RecommendationSection(
                    shows = uiState.shows,
                    onShowClick = onShowClick,
                )
            }

            // section divider
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(Muted.copy(alpha = 0.6f))) }

            // 5. Discount (resale items with discount)
            item {
                ResaleDiscountSection(
                    resaleItems = uiState.resaleItems,
                    onItemClick = { onShowClick(it) },
                )
                Spacer(Modifier.height(32.dp))
            }

            // 🧪 좌석 배치도 테스트 (하단 숨김)
            item {
                TextButton(
                    onClick = { onSeatMapTest("evt_001") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    Text(
                        "🧪 좌석맵 테스트",
                        fontSize = 11.sp,
                        color = MutedForeground.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

// ── Hero Banner (aspect 4:3, title→subtitle→venue→dates, counter pill) ──

@Composable
private fun HeroBanner(slides: List<BannerSlide>, onSlideClick: (String) -> Unit) {
    if (slides.isEmpty()) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(Muted))
        return
    }
    val pagerState = rememberPagerState { slides.size }
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }

    Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
        HorizontalPager(state = pagerState) { page ->
            val slide = slides[page]
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable { onSlideClick(slide.showId) },
            ) {
                AsyncImage(
                    slide.image,
                    slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // gradient overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0x33000000),
                                    Color(0xB3000000),
                                ),
                            ),
                        ),
                )
                // text: title → subtitle → venue → dates (each on separate line)
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(
                        slide.title,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp,
                    )
                    Text(
                        slide.subtitle,
                        color = White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        slide.venue,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        slide.dates,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )
                }
            }
        }

        // counter pill: bottom-right
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .background(Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                "${pagerState.currentPage + 1} / ${slides.size}",
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // dot indicators: bottom-center
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(slides.size) { i ->
                Box(
                    Modifier
                        .size(
                            width = if (i == pagerState.currentPage) 16.dp else 6.dp,
                            height = 6.dp,
                        )
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (i == pagerState.currentPage) White
                            else White.copy(alpha = 0.4f),
                        ),
                )
            }
        }
    }
}

// ── Section Header ("전체보기" + chevron) ──

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
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnBackground)
        if (onMore != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onMore() }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("전체보기", fontSize = 12.sp, color = MutedForeground)
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MutedForeground,
                )
            }
        }
    }
}

// ── Ranking (w-32 h-44 posters) ──

@Composable
private fun RankingSection(items: List<RankingItem>, onItemClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items.take(5)) { item ->
            val rankBg = when (item.rank) {
                1 -> Color(0xFFEAB308)
                2 -> Color(0xFF9CA3AF)
                3 -> Color(0xFFB45309)
                else -> OnBackground
            }
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .clickable { onItemClick(item.showId) },
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box {
                    AsyncImage(
                        item.poster,
                        item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(128.dp)
                            .height(176.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Muted),
                    )
                    Box(
                        Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(rankBg)
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${item.rank}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                        )
                    }
                }
                Text(
                    item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                Text(item.venue, fontSize = 10.sp, color = MutedForeground)
            }
        }
    }
}

// ── Open Schedule (horizontal scroll cards, bg secondary) ──

@Composable
private fun OpenScheduleSection(
    items: List<OpenScheduleItem>,
    onItemClick: (String) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val cardWidth = minOf((screenWidth * 0.8f).toInt(), 300).dp

    Column(
        Modifier
            .background(Muted.copy(alpha = 0.4f))
            .padding(vertical = 20.dp),
    ) {
        HomeSectionHeader(title = "오픈 예정", onMore = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .width(cardWidth)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(12.dp),
                ) {
                    AsyncImage(
                        item.poster,
                        item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 80.dp, height = 96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Muted),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
                        Text(
                            item.openLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isToday) Primary else Info,
                        )
                        Text(
                            item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            item.openType,
                            fontSize = 12.sp,
                            color = MutedForeground,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.tags.forEach { tag ->
                                val isHot = tag == "HOT"
                                Text(
                                    tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHot) Danger else Primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isHot) Danger.copy(alpha = 0.1f)
                                            else Primary.copy(alpha = 0.1f),
                                        )
                                        .border(
                                            1.dp,
                                            if (isHot) Danger.copy(alpha = 0.2f)
                                            else Primary.copy(alpha = 0.2f),
                                            RoundedCornerShape(6.dp),
                                        )
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

// ── Recommendation Section (wishlist card + recommended show) ──

@Composable
private fun RecommendationSection(shows: List<Show>, onShowClick: (String) -> Unit) {
    val wishlistCount = 3 // mock
    val recommendedShow = shows.firstOrNull() ?: return

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "추천 공연")
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Wishlist card
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .height(144.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Primary.copy(alpha = 0.2f),
                                Primary.copy(alpha = 0.1f),
                                Muted,
                            ),
                        ),
                    )
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable {},
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "찜",
                            tint = Primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "찜한 공연",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Text(
                            "$wishlistCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }

            // Recommended show card
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .clickable { onShowClick(recommendedShow.id) }
                    .padding(12.dp),
            ) {
                AsyncImage(
                    recommendedShow.poster,
                    recommendedShow.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 80.dp, height = 112.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Muted),
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                ) {
                    Text(
                        recommendedShow.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                    Text(
                        recommendedShow.date,
                        fontSize = 12.sp,
                        color = MutedForeground,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        recommendedShow.venue.split(",").first().trim(),
                        fontSize = 12.sp,
                        color = MutedForeground,
                    )
                    if (recommendedShow.grades.isNotEmpty()) {
                        Text(
                            "%,d CTK~".format(recommendedShow.grades.first().price),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Discount Section (resale items with discount, vertical list) ──

@Composable
private fun ResaleDiscountSection(
    resaleItems: List<ResaleItem>,
    onItemClick: (String) -> Unit,
) {
    val discounted = remember(resaleItems) {
        resaleItems.mapNotNull { item ->
            if (item.originalPrice <= 0) return@mapNotNull null
            val pct = ((item.originalPrice - item.resalePrice).toFloat() / item.originalPrice * 100)
                .roundToInt()
            if (pct > 0) item to pct else null
        }.sortedByDescending { it.second }.take(5)
    }

    if (discounted.isEmpty()) return

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "지금 할인중!", onMore = {})
        Column(Modifier.padding(horizontal = 16.dp)) {
            discounted.forEachIndexed { index, (item, pct) ->
                if (index > 0) {
                    HorizontalDivider(color = BorderColor)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item.showId) }
                        .padding(vertical = 16.dp),
                ) {
                    // Poster with discount badge
                    Box {
                        AsyncImage(
                            item.poster,
                            item.showName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 96.dp, height = 128.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Muted),
                        )
                        Box(
                            Modifier
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Danger)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .align(Alignment.TopStart),
                        ) {
                            Text(
                                "-$pct%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    // Info
                    Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
                        // "2차 거래소" tag
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Muted)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MutedForeground,
                            )
                            Text(
                                "2차 거래소",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedForeground,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            item.showName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                        )
                        Text(
                            item.venue.split(",").first().trim(),
                            fontSize = 12.sp,
                            color = MutedForeground,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "${item.seatLabel} · ${item.grade}",
                            fontSize = 12.sp,
                            color = MutedForeground,
                        )
                        // Prices
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "%,d CTK".format(item.originalPrice),
                                fontSize = 12.sp,
                                color = MutedForeground,
                                textDecoration = TextDecoration.LineThrough,
                            )
                            Text(
                                "%,d CTK".format(item.resalePrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Danger,
                            )
                        }
                    }
                }
            }
        }
    }
}
