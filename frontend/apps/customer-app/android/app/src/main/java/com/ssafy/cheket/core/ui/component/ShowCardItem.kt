package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.util.DateTimeUtils
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryLight

@Composable
fun ShowCardItem(show: Show, onClick: () -> Unit = {}) {
    val isCompleted = show.status == ShowStatus.COMPLETED
    val locationText = buildString {
        append(show.venue)
        if (show.region.isNotBlank() && !show.venue.contains(show.region)) {
            append(", ")
            append(show.region)
        }
    }

    // 오픈예정/마감임박 뱃지 텍스트 (달력 우측에 표시)
    val openDateFormatted = show.openDate?.let { DateTimeUtils.formatShortDateTime(it) }
    val closeDateFormatted = show.reservationEndDate?.let { DateTimeUtils.formatShortDateTime(it) }
    val deadlineWithin3Days = show.reservationEndDate?.let {
        try {
            val endDate = java.time.LocalDateTime.parse(it)
            val now = java.time.LocalDateTime.now()
            val daysLeft = java.time.Duration.between(now, endDate).toDays()
            daysLeft in 0..3
        } catch (_: Exception) { false }
    } ?: false

    val dateBadge: Pair<String, Color>? = when (show.status) {
        ShowStatus.UPCOMING -> openDateFormatted?.let { "$it 오픈" to Primary }
        ShowStatus.ON_SALE -> if (deadlineWithin3Days) closeDateFormatted?.let { "$it 마감" to Danger } else null
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCompleted) Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)) // 회색 비활성화 배경
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box {
            AsyncImage(
                model = show.poster,
                contentDescription = show.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(92.dp)
                    .height(122.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Muted)
                    .then(
                        if (isCompleted) Modifier.background(Color.Black.copy(alpha = 0.15f))
                        else Modifier
                    ),
            )
            // 종료 오버레이
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(122.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "종료",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
            // 오픈예정/마감 포스터 하단 오버레이
            dateBadge?.let { (text, color) ->
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(color.copy(alpha = 0.85f))
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = show.name,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) MutedForeground else OnBackground,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            show.artistName
                ?.takeIf { it.isNotBlank() }
                ?.let { artistName ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCompleted) Color(0xFFE5E7EB) else PrimaryLight)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = artistName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) MutedForeground else OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                // 달력 + 공연 날짜 + 우측에 오픈/마감 뱃지
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            append(DateTimeUtils.formatShortDate(show.date))
                            if (show.endDate != null && show.endDate != show.date) {
                                append(" ~ ")
                                append(DateTimeUtils.formatShortDate(show.endDate))
                            }
                        },
                        fontSize = 13.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // (오픈예정/마감 뱃지는 포스터 하단 오버레이로 이동됨)
                    if (false) {
                        Box {
                            Text(text = "", fontSize = 10.sp)
                        }
                    }
                }

                MetadataRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = MutedForeground,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = locationText,
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: @Composable () -> Unit,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        Text(
            text = text,
            fontSize = 13.sp,
            color = MutedForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
