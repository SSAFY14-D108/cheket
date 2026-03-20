package com.ssafy.cheket.features.show

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.model.Grade
import com.ssafy.cheket.core.model.RefundRule
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.core.ui.component.elevatedSurfaceSoft
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── v0 Design Tokens ──
private val V0Background = Color(0xFFF8F8F8)  // bg-gray-50
private val V0Card = Color(0xFFFFFFFF)
private val V0TextPrimary = Color(0xFF111111)
private val V0TextMuted = Color(0xFF5C7A73)
private val V0TextSub = Color(0xFF9CA3AF)
private val V0IconGray = Color(0xFF6B7280)
private val V0WishlistBadgeBg = Color(0xFF9AA4B2)  // gray badge, NOT green
private val V0Red400 = Color(0xFFF87171)

@Composable
fun ShowDetailScreen(
    showId: String,
    onNavigateToDateSelection: (showId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowDetailViewModel = viewModel(factory = ShowDetailViewModel.factory(showId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is ShowDetailViewModel.UiState.Loading -> {
            Box(Modifier.fillMaxSize().background(V0Background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = V0TextMuted)
            }
        }
        is ShowDetailViewModel.UiState.Error -> {
            Box(Modifier.fillMaxSize().background(V0Background), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = V0TextMuted, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    // gradient-border-button style retry
                    Box(
                        modifier = Modifier
                            .gradientBorder(RoundedCornerShape(12.dp))
                            .background(V0Card, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                    ) {
                        TextButton(onClick = { viewModel.loadShow() }) {
                            Text("다시 시도", color = V0TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        is ShowDetailViewModel.UiState.Success -> {
            ShowDetailContent(
                show = state.show,
                isLiked = state.isLiked,
                likeCount = state.likeCount,
                onToggleLike = viewModel::toggleLike,
                onNavigateToDateSelection = onNavigateToDateSelection,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun ShowDetailContent(
    show: Show,
    isLiked: Boolean,
    likeCount: Int,
    onToggleLike: () -> Unit,
    onNavigateToDateSelection: (showId: String) -> Unit,
    onBack: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    // 예매 가능 여부: ON_SALE 상태 + 현재 시각이 예매 기간 내
    val now = remember { LocalDateTime.now() }
    val isInReservationPeriod = remember(show.openDate, show.reservationEndDate) {
        try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val start = show.openDate?.let { LocalDateTime.parse(it, formatter) }
            val end = show.reservationEndDate?.let { LocalDateTime.parse(it, formatter) }
            val afterStart = start == null || !now.isBefore(start)
            val beforeEnd = end == null || !now.isAfter(end)
            afterStart && beforeEnd
        } catch (_: Exception) {
            true // 파싱 실패 시 제한하지 않음
        }
    }
    val canBuy = show.status == ShowStatus.ON_SALE && isInReservationPeriod

    // 버튼 텍스트 결정
    val buttonText = when {
        show.status == ShowStatus.SOLD_OUT -> "매진"
        !isInReservationPeriod -> {
            val start = try {
                show.openDate?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
            } catch (_: Exception) { null }
            if (start != null && now.isBefore(start)) {
                val month = start.monthValue
                val day = start.dayOfMonth
                val hour = start.hour
                val min = start.minute
                "${month}/${day} ${hour}:${String.format("%02d", min)} 오픈"
            } else {
                "예매 마감"
            }
        }
        else -> "예매하기"
    }

    Scaffold(
        topBar = {
            AppHeader(title = "공연 상세", onBack = onBack)
        },
        bottomBar = {
            // v0 CTA: gradient-border-button, text #111111, NOT white on green
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(V0Card)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = { onNavigateToDateSelection(show.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .then(
                            if (canBuy) Modifier.gradientBorder(RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = V0Card,
                        contentColor = V0TextPrimary,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = V0TextSub,
                    ),
                    enabled = canBuy,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                    ),
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (canBuy) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        },
        containerColor = V0Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Poster area: h-60 full-width background image + overlays ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            ) {
                // background image (scaled up slightly, blurred effect)
                AsyncImage(
                    model = show.poster,
                    contentDescription = show.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x59000000)), // black/35
                )
                // gradient overlay: from-white/10 via-black/20 to-background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color(0x1AFFFFFF),    // white/10
                                    0.5f to Color(0x33000000),  // black/20
                                    1f to V0Background,         // to-background
                                ),
                            ),
                        ),
                )

                // Small poster card: h-56 w-36 rounded-2xl, floating bottom-left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                        .offset(y = 42.dp)
                        .width(144.dp)
                        .height(224.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                ) {
                    AsyncImage(
                        model = show.poster,
                        contentDescription = show.name,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                    )
                    // 하단 그라데이션: 이미지 아래 빈 공간을 배경색으로 자연스럽게 채움
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.4f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        V0Background,
                                    ),
                                ),
                            ),
                    )
                }

                // Wishlist button: h-11 w-11 rounded-full bg-black/45 (top-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                ) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Black.copy(alpha = 0.45f)),
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isLiked) "찜 해제" else "찜하기",
                            tint = if (isLiked) Danger else White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    // count badge: bg-[#9aa4b2] (gray, NOT green)
                    if (likeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .heightIn(min = 20.dp)
                                .widthIn(min = 20.dp)
                                .clip(CircleShape)
                                .background(V0WishlistBadgeBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$likeCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
            }

            // ── Content area below poster (with top padding to account for floating poster) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 64.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Title + artist + info
                Column {
                    // text-2xl font-bold (v0: text-lg font-bold)
                    Text(
                        text = show.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = V0TextPrimary,
                        lineHeight = 24.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (show.artistName != null && show.artistName != "대중음악" && show.artistName != "정보 없음") {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = show.artistName,
                            fontSize = 14.sp,
                            color = V0TextMuted,
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Info icons: Calendar, MapPin, Users - text-sm text-muted-foreground
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = V0IconGray, modifier = Modifier.size(16.dp))
                            Text(show.date, fontSize = 14.sp, color = V0TextMuted)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.LocationOn, null, tint = V0IconGray, modifier = Modifier.size(16.dp))
                            Text(show.venue, fontSize = 14.sp, color = V0TextMuted)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.People, null, tint = V0IconGray, modifier = Modifier.size(16.dp))
                            Text("1인당 최대 ${show.maxPerUser}매 구매 가능", fontSize = 14.sp, color = V0TextMuted)
                        }
                    }
                }

                // Description
                if (show.description != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .elevatedSurfaceSoft(RoundedCornerShape(12.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            text = show.description,
                            fontSize = 14.sp,
                            color = V0TextMuted,
                            lineHeight = 22.sp,
                        )
                    }
                }

                // ── Grades / Pricing (v0: elevated-surface-soft rounded-xl px-4 py-3 per grade) ──
                Column {
                    Text(
                        text = "가격 안내",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        show.grades.forEach { grade ->
                            GradeRow(grade = grade, numberFormat = numberFormat)
                        }
                    }
                }

                // ── Description Images (v0: 공연 소개 이미지) ──
                if (show.descriptionImages.isNotEmpty()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                tint = V0IconGray,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                "공연 소개 이미지",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = V0TextPrimary,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .elevatedSurface(RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp)),
                        ) {
                            show.descriptionImages.forEach { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "공연 소개 이미지",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }

                // ── Refund Rules (v0: elevated-surface-soft rounded-xl p-4) ──
                if (show.refundRules.isNotEmpty()) {
                    RefundRulesSection(refundRules = show.refundRules)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Grade Row (v0: each grade in its own elevated-surface-soft rounded-xl card) ──
@Composable
private fun GradeRow(
    grade: Grade,
    numberFormat: NumberFormat,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurfaceSoft(RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // grade name: text-sm font-semibold text-[#111111]
            Text(
                text = grade.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0TextPrimary,
            )
            // remaining: text-xs text-muted or text-red-400
            if (grade.remaining != null) {
                Text(
                    text = if (grade.remaining == 0) "매진" else "잔여 ${grade.remaining}석",
                    fontSize = 12.sp,
                    color = if (grade.remaining == 0) V0Red400 else V0TextMuted,
                )
            }
        }
        // price: text-sm font-bold text-[#111111]
        Text(
            text = "${numberFormat.format(grade.price)} CTK",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = V0TextPrimary,
        )
    }
}

// ── Refund Rules (v0: elevated-surface rounded-xl p-4) ──
@Composable
private fun RefundRulesSection(
    refundRules: List<RefundRule>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .elevatedSurface(RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        // header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            Icon(
                Icons.Outlined.Receipt,
                contentDescription = null,
                tint = V0IconGray,
                modifier = Modifier.size(16.dp),
            )
            Text(
                "환불 규정",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0TextPrimary,
            )
        }
        // rules list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            refundRules.forEach { rule ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(rule.label, fontSize = 14.sp, color = V0TextMuted)
                    Text(
                        if (rule.feeRate >= 1f) "환불 불가"
                        else "수수료 ${(rule.feeRate * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = V0TextMuted,
                    )
                }
            }
        }
    }
}
