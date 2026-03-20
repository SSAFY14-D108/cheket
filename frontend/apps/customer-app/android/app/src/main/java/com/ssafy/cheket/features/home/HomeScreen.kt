package com.ssafy.cheket.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
private val V0SectionDivider = Color(0xFFF3F4F6) // gray-100
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
            // Simple top bar - no Material elevation shadow
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
                        contentDescription = "CHEKET 로고",
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
                .background(Color(0xFFF9FAFB)) // bg-gray-50
                .padding(innerPadding),
        ) {
            // 1. Hero Banner
            item {
                HeroBanner(
                    slides = uiState.bannerSlides,
                    onSlideClick = { onShowClick(it) },
                )
            }

            // 2. Ranking
            item {
                Column(Modifier.padding(top = 20.dp, bottom = 16.dp)) {
                    HomeSectionHeader(title = "랭킹")
                    RankingSection(
                        items = uiState.rankingItems,
                        onItemClick = { onShowClick(it) },
                    )
                }
            }

            // section divider (gray-100)
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(V0SectionDivider)) }

            // 3. Open Schedule
            item {
                OpenScheduleSection(
                    items = uiState.openSchedule,
                    onItemClick = { onShowClick(it) },
                )
            }

            // section divider
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(V0SectionDivider)) }

            // 4. Recommendation
            item {
                RecommendationSection(
                    shows = uiState.shows,
                    onShowClick = onShowClick,
                )
            }

            // section divider
            item { Box(Modifier.fillMaxWidth().height(8.dp).background(V0SectionDivider)) }

            // 5. Discount (resale items with discount)
            item {
                ResaleDiscountSection(
                    resaleItems = uiState.resaleItems,
                    onItemClick = { onShowClick(it) },
                )
                Spacer(Modifier.height(32.dp))
            }

            // seat map test (hidden at bottom)
            item {
                TextButton(
                    onClick = { onSeatMapTest("evt_001") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    Text(
                        "좌석맵 테스트",
                        fontSize = 11.sp,
                        color = V0TextSub.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

// ── Hero Banner (aspect 4:3, gradient from-black/70 via-black/20 to-transparent, counter pill) ──

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
                // v0 gradient: from-black/70 via-black/20 to-transparent (bottom to top)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.4f to Color(0x33000000), // black/20
                                    1f to Color(0xB3000000),   // black/70
                                ),
                            ),
                        ),
                )
                // text overlay: bottom-left
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    // text-xl font-black white
                    Text(
                        slide.title,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp,
                    )
                    // text-sm font-bold text-white/90
                    Text(
                        slide.subtitle,
                        color = White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    // text-xs text-white/70
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

        // counter pill: bg-black/50 rounded-full backdrop-blur (bottom-right)
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

// ── Section Header (v0: text-base font-bold + "더보기" text link with ChevronRight) ──

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
        // text-base font-bold text-foreground
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = V0TextPrimary)
        if (onMore != null) {
            // text link: text-xs text-muted-foreground + ChevronRight icon
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

// ── Ranking Section (v0: elevated-surface-soft rounded-2xl cards) ──

@Composable
private fun RankingSection(items: List<RankingItem>, onItemClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items.take(5)) { item ->
            // v0 rank badge colors: gold/silver/bronze
            val rankBg = when (item.rank) {
                1 -> Color(0xFFEAB308)  // yellow-500
                2 -> Color(0xFF9CA3AF)  // gray-400
                3 -> Color(0xFFB45309)  // amber-700
                else -> Color(0xFF374151)  // gray-700
            }
            // elevated-surface-soft rounded-2xl card
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                    .clickable { onItemClick(item.showId) }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // poster with rank badge
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
                    // rank badge: circle with gold/silver/bronze
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
                // title: text-xs font-semibold text-gray-900
                Text(
                    item.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0Gray900,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                // venue: text-[10px] text-gray-500
                Text(
                    item.venue,
                    fontSize = 10.sp,
                    color = V0Gray500,
                )
            }
        }
    }
}

// ── Open Schedule (v0: elevated-surface-soft horizontal scroll cards) ──

@Composable
private fun OpenScheduleSection(
    items: List<OpenScheduleItem>,
    onItemClick: (String) -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val cardWidth = minOf((screenWidth * 0.8f).toInt(), 300).dp

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "오픈 예정", onMore = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items.size) { index ->
                val item = items[index]
                // elevated-surface-soft rounded-2xl card
                Row(
                    modifier = Modifier
                        .width(cardWidth)
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(12.dp),
                ) {
                    // poster: h-24 w-20 rounded-xl
                    AsyncImage(
                        item.poster,
                        item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 80.dp, height = 96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                    ) {
                        // open label: first item #111111, others #4b5563
                        Text(
                            item.openLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index == 0) V0TextPrimary else Color(0xFF4B5563),
                        )
                        // name: text-sm font-semibold text-gray-900
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
                        // venue: text-xs text-gray-500
                        Text(
                            item.openType,
                            fontSize = 12.sp,
                            color = V0Gray500,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        // tags
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

// ── Recommendation Section (v0: wishlist card + recommended show) ──

@Composable
private fun RecommendationSection(shows: List<Show>, onShowClick: (String) -> Unit) {
    val wishlistCount = 3 // mock
    val recommendedShow = shows.firstOrNull() ?: return

    Column(Modifier.padding(vertical = 20.dp)) {
        HomeSectionHeader(title = "취향 저격 추천")
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Wishlist card: elevated-surface-soft, h-36 w-28
            Column(
                modifier = Modifier
                    .width(112.dp)
                    .height(144.dp)
                    .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                    .clickable {},
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // circle icon bg: bg-gray-100
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "찜",
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                // text-xs font-bold text-gray-900
                Text(
                    "찜한 공연",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0Gray900,
                )
                // text-lg font-bold text-[#111111]
                Text(
                    "$wishlistCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0TextPrimary,
                )
            }

            // Recommended show card: elevated-surface-soft
            Row(
                modifier = Modifier
                    .weight(1f)
                    .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                    .clickable { onShowClick(recommendedShow.id) }
                    .padding(12.dp),
            ) {
                // poster: h-28 w-20 rounded-xl
                AsyncImage(
                    recommendedShow.poster,
                    recommendedShow.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 80.dp, height = 112.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6)),
                )
                Spacer(Modifier.width(12.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp),
                ) {
                    // name: text-sm font-semibold text-gray-900
                    Text(
                        recommendedShow.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0Gray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                    )
                    // date: text-xs text-gray-500
                    Text(
                        recommendedShow.date,
                        fontSize = 12.sp,
                        color = V0Gray500,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    // venue: text-xs text-gray-500
                    Text(
                        recommendedShow.venue.split(",").first().trim(),
                        fontSize = 12.sp,
                        color = V0Gray500,
                    )
                    // price: text-xs font-medium text-[#333333]
                    if (recommendedShow.grades.isNotEmpty()) {
                        val lowestPrice = recommendedShow.grades
                            .filter { it.price > 0 }
                            .minOfOrNull { it.price }
                        if (lowestPrice != null) {
                            Text(
                                "%,d CTK~".format(lowestPrice),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF333333),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Discount Section (v0: vertical list, elevated-surface-soft cards) ──

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
                // elevated-surface-soft rounded-2xl card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .elevatedSurfaceSoft(RoundedCornerShape(16.dp))
                        .clickable { onItemClick(item.showId) }
                        .padding(16.dp),
                ) {
                    // Poster with discount badge: h-32 w-24 rounded-xl
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
                        // discount badge: bg-red-500 text-white
                        Box(
                            Modifier
                                .padding(6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(V0Red500)
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
                    // Info column
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(vertical = 2.dp),
                    ) {
                        // "2차 거래" tag with icon
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
                            Text(
                                "2차 거래",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = V0TextMuted,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        // name: text-sm font-semibold text-gray-900
                        Text(
                            item.showName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = V0Gray900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp,
                        )
                        // venue: text-xs text-gray-500
                        Text(
                            item.venue.split(",").first().trim(),
                            fontSize = 12.sp,
                            color = V0Gray500,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        // seat + grade
                        Text(
                            "${item.seatLabel} · ${item.grade}",
                            fontSize = 12.sp,
                            color = V0Gray500,
                        )
                        // prices: original (strikethrough gray-400) + resale (bold red-500)
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
