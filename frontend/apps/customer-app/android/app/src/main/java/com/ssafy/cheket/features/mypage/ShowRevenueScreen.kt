package com.ssafy.cheket.features.mypage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.RevenueSplitInfo
import com.ssafy.cheket.core.network.dto.RevenueSplitResponse
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryLight
import com.ssafy.cheket.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

private const val TAG = "ShowRevenueScreen"

private val OrganizerColor = Color(0xFF6366F1)  // indigo
private val ArtistColor = Primary               // green
private val PlatformColor = Color(0xFFF59E0B)   // amber

@Composable
fun ShowRevenueScreen(
    showId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CheketApplication }
    val showService = remember { app.appContainer.showService }

    var isLoading by remember { mutableStateOf(true) }
    var revenue by remember { mutableStateOf<RevenueSplitResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showId) {
        try {
            val response = showService.getRevenueSplit(showId)
            if (response.httpStatusCode in 200..299 && response.data != null) {
                revenue = response.data
                Log.d(TAG, "Revenue loaded: total=${response.data.totalRevenue}")
            } else {
                Log.w(TAG, "API returned ${response.httpStatusCode}, using mock")
                revenue = mockRevenueSplit(showId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "API not available, using mock", e)
            revenue = mockRevenueSplit(showId)
        }
        isLoading = false
    }

    Scaffold(
        topBar = { AppHeader(title = "매출 / 정산", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                }
            }

            revenue == null -> {
                EmptyState(
                    title = "정산 정보를 불러오지 못했어요",
                    description = error ?: "잠시 후 다시 시도해주세요.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                val data = revenue!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 공연 제목
                    Text(
                        text = data.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )

                    // 총 매출 카드
                    TotalRevenueCard(totalRevenue = data.totalRevenue)

                    // 수익 배분 카드
                    RevenueSplitCard(splits = data.splits, totalRevenue = data.totalRevenue)

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TotalRevenueCard(totalRevenue: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "총 매출",
            fontSize = 13.sp,
            color = MutedForeground,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "${formatAmount(totalRevenue)} SSF",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackground,
        )
    }
}

@Composable
private fun RevenueSplitCard(splits: List<RevenueSplitInfo>, totalRevenue: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "수익 배분 내역",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackground,
        )

        splits.forEachIndexed { index, split ->
            if (index > 0) {
                HorizontalDivider(color = BorderColor)
            }
            SplitRow(split = split, totalRevenue = totalRevenue)
        }

        // 합계
        HorizontalDivider(color = OnBackground.copy(alpha = 0.2f), thickness = 1.5.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "합계",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatBps(splits.sumOf { it.rateBps })}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedForeground,
                )
                Text(
                    text = "${formatAmount(splits.sumOf { it.amount })} SSF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
            }
        }
    }
}

@Composable
private fun SplitRow(split: RevenueSplitInfo, totalRevenue: Double) {
    val roleColor = when (split.role) {
        "ORGANIZER" -> OrganizerColor
        "ARTIST" -> ArtistColor
        else -> PlatformColor
    }
    val roleLabel = when (split.role) {
        "ORGANIZER" -> "주최"
        "ARTIST" -> "아티스트"
        else -> split.role
    }
    val roleIcon = when (split.role) {
        "ORGANIZER" -> Icons.Outlined.Business
        "ARTIST" -> Icons.Outlined.Person
        else -> Icons.Outlined.AccountBalance
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(roleColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = roleIcon,
                contentDescription = null,
                tint = roleColor,
                modifier = Modifier.size(20.dp),
            )
        }

        // 이름/역할
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = split.name ?: "알 수 없음",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground,
            )
            Text(
                text = roleLabel,
                fontSize = 12.sp,
                color = roleColor,
                fontWeight = FontWeight.Medium,
            )
        }

        // 비율 + 금액
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatBps(split.rateBps),
                fontSize = 13.sp,
                color = MutedForeground,
            )
            Text(
                text = "${formatAmount(split.amount)} SSF",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
        }
    }
}

private fun formatAmount(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(amount.toLong())

private fun formatBps(bps: Int): String {
    val pct = bps / 100.0
    return if (pct == pct.toLong().toDouble()) "${pct.toLong()}%"
    else "${"%.1f".format(pct)}%"
}

private fun mockRevenueSplit(showId: Long) = RevenueSplitResponse(
    showId = showId,
    title = "CHEKET 콘서트",
    totalRevenue = 5000000.0,
    splits = listOf(
        RevenueSplitInfo(
            role = "ORGANIZER",
            id = 1,
            name = "체켓 엔터테인먼트",
            rateBps = 5500,
            amount = 2750000.0,
        ),
        RevenueSplitInfo(
            role = "ARTIST",
            id = 5,
            name = "CHEKET BAND",
            rateBps = 4000,
            amount = 2000000.0,
        ),
        RevenueSplitInfo(
            role = "PLATFORM",
            id = null,
            name = "플랫폼 수수료",
            rateBps = 500,
            amount = 250000.0,
        ),
    ),
)
