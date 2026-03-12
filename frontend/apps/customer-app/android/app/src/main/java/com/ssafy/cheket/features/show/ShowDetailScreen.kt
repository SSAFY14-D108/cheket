package com.ssafy.cheket.features.show

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
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
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.model.Grade
import com.ssafy.cheket.core.model.RefundRule
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.ShowStatusBadge
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
        is ShowDetailViewModel.UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MutedForeground, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadShow() }) {
                        Text("다시 시도")
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

    Scaffold(
        topBar = {
            AppHeader(title = "공연 상세", onBack = onBack)
        },
        bottomBar = {
            // Sticky CTA button (v0 스타일)
            Surface(shadowElevation = 8.dp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBg)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Button(
                        onClick = { onNavigateToDateSelection(show.id) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (show.status == ShowStatus.ON_SALE) Primary else MutedForeground,
                            contentColor = White,
                        ),
                        enabled = show.status == ShowStatus.ON_SALE,
                    ) {
                        Text(
                            text = when (show.status) {
                                ShowStatus.ON_SALE -> "예매하기"
                                ShowStatus.SOLD_OUT -> "매진"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        if (show.status == ShowStatus.ON_SALE) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Poster section - 3:2 aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
            ) {
                AsyncImage(
                    model = show.poster,
                    contentDescription = show.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC000000))
                            )
                        )
                )
                // Status badge - bottom left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    ShowStatusBadge(status = show.status)
                }
                // Wishlist button - top right with count badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    IconButton(
                        onClick = onToggleLike,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isLiked) "찜 해제" else "찜하기",
                            tint = if (isLiked) Danger else White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    if (likeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$likeCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                            )
                        }
                    }
                }
            }

            // Show title & info - below poster
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = show.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (show.artistName != null && show.artistName != "대중음악") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = show.artistName,
                        fontSize = 14.sp,
                        color = MutedForeground,
                    )
                }
                Spacer(Modifier.height(12.dp))

                // Info rows with icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(show.date, fontSize = 14.sp, color = MutedForeground)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(show.venue, fontSize = 14.sp, color = MutedForeground)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text("1인 최대 ${show.maxPerUser}매", fontSize = 14.sp, color = MutedForeground)
                }
            }

            // Description section
            if (show.description != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "공연 소개",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Muted,
                    ) {
                        Text(
                            text = show.description,
                            fontSize = 14.sp,
                            color = MutedForeground,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            // Grades / Pricing section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "가격 안내",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBg,
                    shadowElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        show.grades.forEachIndexed { index, grade ->
                            GradeRow(grade = grade, numberFormat = numberFormat)
                            if (index < show.grades.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderColor,
                                )
                            }
                        }
                    }
                }
            }

            // Refund Rules
            if (show.refundRules.isNotEmpty()) {
                RefundRulesSection(
                    refundRules = show.refundRules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GradeRow(
    grade: Grade,
    numberFormat: NumberFormat,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = grade.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground,
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${numberFormat.format(grade.price)} CTK",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Text(
                text = if (grade.remaining > 0) "잔여 ${grade.remaining}석" else "매진",
                fontSize = 11.sp,
                color = if (grade.remaining > 0) Primary else Danger,
                fontWeight = if (grade.remaining == 0) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun RefundRulesSection(
    refundRules: List<RefundRule>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "환불 규정",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                )
            }
            refundRules.forEachIndexed { index, rule ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(rule.label, fontSize = 14.sp, color = MutedForeground)
                    Text(
                        if (rule.feeRate >= 1f) "환불 불가"
                        else "수수료 ${(rule.feeRate * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = MutedForeground,
                    )
                }
            }
        }
    }
}
