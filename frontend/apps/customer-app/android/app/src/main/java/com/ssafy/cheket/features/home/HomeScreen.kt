package com.ssafy.cheket.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.R
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ── v0 Design Tokens ──
private val V0Background = Color(0xFFFCFCFC)
private val V0Card = Color(0xFFFFFFFF)
private val V0Border = Color(0xFFD8EFEA)
private val V0TextPrimary = Color(0xFF111111)
private val V0TextMuted = Color(0xFF5C7A73)
private val V0TextSub = Color(0xFF9CA3AF)
private val V0ActiveFilterBg = Color(0xFFEEF2F1)
private val V0SectionDivider = Color(0xFFF3F4F6)
private val V0Gray500 = Color(0xFF6B7280)
private val V0Gray900 = Color(0xFF111827)
private val V0Red500 = Color(0xFFEF4444)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(V0Card)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cheket_logo2),
                        contentDescription = "CHEKET",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(32.dp),
                    )
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "알림", tint = V0TextPrimary)
                        }
                        IconButton(onClick = onMyPage) {
                            Icon(Icons.Filled.Person, contentDescription = "마이페이지", tint = V0TextPrimary)
                        }
                    }
                }
            }
        },
        containerColor = V0Background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(innerPadding),
        ) {
            // 1. AI 추천 배너
            item {
                HeroBanner(
                    slides = uiState.bannerSlides,
                    onSlideClick = { onShowClick(it) },
                )
            }

            // 2. 랭킹
            if (uiState.rankingItems.isNotEmpty()) {
                item {
                    Column(Modifier.padding(top = 20.dp, bottom = 16.dp)) {
                        HomeSectionHeader(title = "랭킹")
                        RankingSection(
                            items = uiState.rankingItems,
                            onItemClick = { onShowClick(it) },
                        )
                    }
                }
                item { SectionDivider() }
            }

            // 3. 오픈 예정
            if (uiState.openSchedule.isNotEmpty()) {
                item {
                    OpenScheduleSection(
                        items = uiState.openSchedule,
                        onItemClick = { onShowClick(it) },
                    )
                }
                item { SectionDivider() }
            }

            // 4. 찜한 공연
            if (uiState.likedShows.isNotEmpty()) {
                item {
                    LikedShowsSection(
                        likedShows = uiState.likedShows,
                        onShowClick = onShowClick,
                    )
                }
                item { SectionDivider() }
            }

            // 5. 타임 세일 (리세일 할인)
            if (uiState.resaleItems.isNotEmpty()) {
                item {
                    ResaleDiscountSection(
                        resaleItems = uiState.resaleItems,
                        onItemClick = { onShowClick(it) },
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(Modifier.fillMaxWidth().height(8.dp).background(V0SectionDivider))
}

// ── Hero Banner (AI 추천 auto-carousel) ──

@Composable
private fun HeroBanner(slides: List<BannerSlide>, onSlideClick: (String) -> Unit) {
    if (slides.isEmpty()) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(Color(0xFFE5E7EB)))
        return
    }
    val pagerState = rememberPagerState { slides.size }
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            if (slides.size > 1) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
            }
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

        if (slides.size > 1) {
            // counter pill
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
            // dot indicators
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
}

// ── Section Header ──

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
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = V0TextPrimary)
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
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = V0TextMuted,
                )
            }
        }
    }
}

// ── Ranking (인기순 Top 5) ──

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
                else -> Color(0xFF374151)
            }
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                    .clickable { onItemClick(item.showId) }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box {
                    AsyncImage(
                        item.poster,
                        item.name,
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
                            .background(rankBg)
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${item.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
                Text(
                    item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0Gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                Text(item.venue, fontSize = 10.sp, color = V0Gray500)
            }
        }
    }
}

// ── Open Schedule (오픈 예정 — 카드 개선) ──

@Composable
private fun OpenScheduleSection(
    items: List<OpenScheduleItem>,
    onItemClick: (String) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val cardWidth = minOf((screenWidth * 0.82f).toInt(), 320).dp

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "오픈 예정", onMore = {})
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
                    // 포스터
                    AsyncImage(
                        item.poster,
                        item.name,
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
                        // 오픈 날짜/시간 (핵심 정보)
                        if (item.openLabel.isNotBlank()) {
                            Text(
                                item.openLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index == 0) Primary else Color(0xFF4B5563),
                                maxLines = 1,
                            )
                        }

                        // 공연명
                        Text(
                            item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Gray900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )

                        // 장소
                        if (item.venue.isNotBlank()) {
                            Text(
                                item.venue,
                                fontSize = 12.sp,
                                color = V0Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }

                        // 공연 일정
                        val dateLabel = buildShowDateLabel(item.showDate, item.showEndDate)
                        if (dateLabel.isNotBlank()) {
                            Text(
                                dateLabel,
                                fontSize = 11.sp,
                                color = V0TextSub,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // 태그 (지역 등)
                        if (item.tags.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.tags.forEach { tag ->
                                    Text(
                                        tag,
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

/** "2026-05-03" ~ "2026-05-05" → "공연 5/3 ~ 5/5" */
private fun buildShowDateLabel(startDate: String, endDate: String): String {
    if (startDate.isBlank()) return ""
    return try {
        val start = startDate.split("-")
        val m1 = start[1].toInt()
        val d1 = start[2].toInt()
        if (endDate.isNotBlank() && endDate != startDate) {
            val end = endDate.split("-")
            val m2 = end[1].toInt()
            val d2 = end[2].toInt()
            "공연 $m1/$d1 ~ $m2/$d2"
        } else {
            "공연 $m1/$d1"
        }
    } catch (_: Exception) {
        ""
    }
}

// ── 찜한 공연 (Liked Shows) ──

@Composable
private fun LikedShowsSection(
    likedShows: List<LikedShow>,
    onShowClick: (String) -> Unit,
) {
    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "찜한 공연")

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
                    // 포스터 + 하트 아이콘
                    Box {
                        AsyncImage(
                            show.posterUrl,
                            show.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                        )
                        // 하트 배지
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
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    // 공연명
                    Text(
                        show.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )
                    // 장소
                    Text(
                        show.venue,
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

// ── Resale Discount (타임 세일) ──

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
        HomeSectionHeader(title = "타임 세일", onMore = {})
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            discounted.forEach { (item, pct) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(16.dp),
                ) {
                    Box {
                        AsyncImage(
                            item.poster,
                            item.showName,
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
                            Text("-$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = White)
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
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = V0TextMuted,
                            )
                            Text("2차 거래", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = V0TextMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            item.showName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Gray900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                        )
                        Text(
                            item.venue.split(",").first().trim(),
                            fontSize = 12.sp,
                            color = V0Gray500,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Text(
                            "${item.seatLabel} · ${item.grade}",
                            fontSize = 12.sp,
                            color = V0Gray500,
                        )
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "%,d CTK".format(item.originalPrice),
                                fontSize = 12.sp,
                                color = V0TextSub,
                                textDecoration = TextDecoration.LineThrough,
                            )
                            Text(
                                "%,d CTK".format(item.resalePrice),
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
