package com.ssafy.cheket.features.event

import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.EventStatus
import com.ssafy.cheket.core.model.Grade
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EventStatusBadge
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateToQueue: (eventId: String) -> Unit,
    onBack: () -> Unit,
) {
    val event = remember { MockDataSource.mockEvents.find { it.id == eventId } }
    var isWishlisted by remember { mutableStateOf(false) }

    if (event == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("이벤트를 찾을 수 없습니다.", color = MutedForeground, fontSize = 16.sp)
        }
        return
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Scaffold(
        topBar = {
            AppHeader(title = "공연 상세", onBack = onBack)
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
                    model = event.poster,
                    contentDescription = event.name,
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
                    EventStatusBadge(status = event.status)
                }
                // Wishlist button - top right
                IconButton(
                    onClick = { isWishlisted = !isWishlisted },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isWishlisted) "찜 해제" else "찜하기",
                        tint = if (isWishlisted) Danger else White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            // Event title & info - below poster
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = event.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(12.dp))

                // Info rows with icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(event.date, fontSize = 14.sp, color = MutedForeground)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text(event.venue, fontSize = 14.sp, color = MutedForeground)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text("1인 최대 ${event.maxPerUser}매", fontSize = 14.sp, color = MutedForeground)
                }
            }

            // Description section
            if (event.description != null) {
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
                            text = event.description,
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
                    text = "등급별 가격",
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
                        event.grades.forEachIndexed { index, grade ->
                            GradeRow(grade = grade, numberFormat = numberFormat)
                            if (index < event.grades.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = BorderColor,
                                )
                            }
                        }
                    }
                }
            }

            // CTA Button - inline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = { onNavigateToQueue(event.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (event.status == EventStatus.ON_SALE) Primary else MutedForeground,
                        contentColor = White,
                    ),
                    enabled = event.status == EventStatus.ON_SALE,
                ) {
                    Text(
                        text = when (event.status) {
                            EventStatus.ON_SALE -> "예매하기"
                            EventStatus.SOLD_OUT -> "매진"
                            EventStatus.ENDED -> "종료된 공연"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (event.status == EventStatus.ON_SALE) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
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
