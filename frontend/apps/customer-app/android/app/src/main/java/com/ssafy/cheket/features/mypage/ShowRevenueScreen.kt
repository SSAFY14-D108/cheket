package com.ssafy.cheket.features.mypage

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.RevenueSplitOnchainResponse
import com.ssafy.cheket.core.network.dto.SplitOnchainInfo
import com.ssafy.cheket.core.network.dto.StakeholderOnchainInfo
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

private const val TAG = "ShowRevenueScreen"

private val OrganizerColor = Color(0xFF6366F1)
private val ArtistColor = Color(0xFF10B981)
private val PlatformColor = Color(0xFFF59E0B)
private val OnchainAccent = Color(0xFF00C598)
private val OnchainBg = Color(0xFF0D1B2A)
private val OnchainCardBg = Color(0xFF122036)
private val OnchainBorder = Color(0xFF1E3A5F)

@Composable
fun ShowRevenueScreen(
    showId: Long,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CheketApplication }
    val showService = remember { app.appContainer.showService }

    var isLoading by remember { mutableStateOf(true) }
    var revenue by remember { mutableStateOf<RevenueSplitOnchainResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showId) {
        try {
            val response = showService.getRevenueSplitOnchain(showId)
            if (response.httpStatusCode in 200..299 && response.data != null) {
                revenue = response.data
                Log.d(TAG, "Revenue loaded: total=${response.data.totalRevenue}")
            } else {
                Log.w(TAG, "Onchain API returned ${response.httpStatusCode}, falling back to DB")
                val fallback = showService.getRevenueSplit(showId)
                if (fallback.httpStatusCode in 200..299 && fallback.data != null) {
                    revenue = RevenueSplitOnchainResponse(
                        showId = fallback.data.showId,
                        title = fallback.data.title,
                        totalRevenue = fallback.data.totalRevenue,
                        splits = fallback.data.splits.map { s ->
                            SplitOnchainInfo(
                                role = s.role, id = s.id, name = s.name,
                                rateBps = s.rateBps, amount = s.amount, onchain = null,
                            )
                        },
                    )
                } else {
                    revenue = mockRevenueSplit(showId)
                }
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
        contentWindowInsets = WindowInsets(0),
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = data.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )

                    TotalRevenueCard(totalRevenue = data.totalRevenue)

                    RevenueSplitCard(splits = data.splits)

                    val hasOnchain = data.splits.any { it.onchain != null }
                    if (hasOnchain) {
                        OnchainVerificationCard(splits = data.splits)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── 총 매출 카드 ──

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

// ── 수익 배분 카드 ──

@Composable
private fun RevenueSplitCard(splits: List<SplitOnchainInfo>) {
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
            SplitRow(split = split)
        }

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
                    text = formatBps(splits.sumOf { it.rateBps }),
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
private fun SplitRow(split: SplitOnchainInfo) {
    val roleColor = when (split.role) {
        "ORGANIZER" -> OrganizerColor
        "ARTIST" -> ArtistColor
        else -> PlatformColor
    }
    val roleLabel = when (split.role) {
        "ORGANIZER" -> "기업"
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

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = split.name ?: "수수료",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                )
                if (split.onchain != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = "온체인 검증됨",
                        tint = OnchainAccent,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                text = roleLabel,
                fontSize = 12.sp,
                color = roleColor,
                fontWeight = FontWeight.Medium,
            )
        }

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

// ── 온체인 검증 카드 ──

@Composable
private fun OnchainVerificationCard(splits: List<SplitOnchainInfo>) {
    val onchainSplits = splits.filter { it.onchain != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(OnchainBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OnchainAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = null,
                    tint = OnchainAccent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = "On-chain 검증",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "블록체인에 기록된 수익 배분 정보",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                )
            }
        }

        onchainSplits.forEachIndexed { index, split ->
            if (index > 0) {
                HorizontalDivider(color = OnchainBorder)
            }
            OnchainSplitDetail(split = split)
        }
    }
}

@Composable
private fun OnchainSplitDetail(split: SplitOnchainInfo) {
    val onchain = split.onchain ?: return
    var expanded by remember { mutableStateOf(false) }

    val roleColor = when (split.role) {
        "ORGANIZER" -> OrganizerColor
        "ARTIST" -> ArtistColor
        else -> PlatformColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(OnchainCardBg)
            .border(1.dp, OnchainBorder, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 이름 + 역할 배지 + 온체인 비율
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = split.name ?: "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = onchain.onchainRole,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = roleColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(roleColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
            Text(
                text = formatBps(onchain.onchainShareBps),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnchainAccent,
            )
        }

        // DB vs 온체인 비율 비교
        val dbBps = split.rateBps
        val chainBps = onchain.onchainShareBps
        val isMatch = dbBps == chainBps

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isMatch) OnchainAccent.copy(alpha = 0.08f)
                    else Color(0xFFF59E0B).copy(alpha = 0.08f),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isMatch) "DB ↔ On-chain 일치" else "DB ↔ On-chain 불일치",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMatch) OnchainAccent else Color(0xFFF59E0B),
            )
            Text(
                text = "${formatBps(dbBps)} / ${formatBps(chainBps)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f),
            )
        }

        // 확장 영역 — 상세 온체인 데이터
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HorizontalDivider(color = OnchainBorder)
                Spacer(modifier = Modifier.height(2.dp))
                OnchainInfoRow(label = "Wallet", value = truncateAddress(onchain.walletAddress))
                OnchainInfoRow(label = "Stakeholder NFT", value = "#${onchain.stakeholderNftId}")
                OnchainInfoRow(label = "Event NFT", value = "#${onchain.eventNftId}")
            }
        }

        // 탭 힌트
        if (!expanded) {
            Text(
                text = "탭하여 상세 보기",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun OnchainInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 0.5.sp,
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.75f),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Utilities ──

private fun formatAmount(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(amount.toLong())

private fun formatBps(bps: Int): String {
    val pct = bps / 100.0
    return if (pct == pct.toLong().toDouble()) "${pct.toLong()}%"
    else "${"%.1f".format(pct)}%"
}

private fun truncateAddress(addr: String): String =
    if (addr.length > 14) "${addr.take(8)}...${addr.takeLast(6)}" else addr

private fun mockRevenueSplit(showId: Long) = RevenueSplitOnchainResponse(
    showId = showId,
    title = "CHEKET 콘서트",
    totalRevenue = 5000000.0,
    splits = listOf(
        SplitOnchainInfo(
            role = "ORGANIZER", id = 1, name = "체켓 엔터테인먼트",
            rateBps = 5500, amount = 2750000.0,
            onchain = StakeholderOnchainInfo(
                stakeholderNftId = 1, walletAddress = "0x1234567890abcdef1234567890abcdef12345678",
                onchainRole = "ORGANIZER", onchainShareBps = 5500, eventNftId = 100,
            ),
        ),
        SplitOnchainInfo(
            role = "ARTIST", id = 5, name = "CHEKET BAND",
            rateBps = 4000, amount = 2000000.0,
            onchain = StakeholderOnchainInfo(
                stakeholderNftId = 2, walletAddress = "0xabcdef1234567890abcdef1234567890abcdef12",
                onchainRole = "ARTIST", onchainShareBps = 4000, eventNftId = 100,
            ),
        ),
        SplitOnchainInfo(
            role = "PLATFORM", id = null, name = "플랫폼 수수료",
            rateBps = 500, amount = 250000.0, onchain = null,
        ),
    ),
)
