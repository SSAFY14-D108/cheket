package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*

@Composable
fun ResaleDetailScreen(
    resaleItemId: String,
    onPurchaseComplete: (String) -> Unit,
    onBack: () -> Unit,
) {
    val resaleItem = remember { MockDataSource.mockResaleItems.find { it.id == resaleItemId } }
    val user = remember { MockDataSource.mockUser }

    if (resaleItem == null) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "재판매 티켓",
                    onBack = onBack,
                    actions = {
                        TutorialHelpButton(tutorialId = TutorialId.RESALE_DETAIL)
                    },
                )
            },
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("해당 리세일 항목을 찾을 수 없습니다.", color = MutedForeground, fontSize = 14.sp)
            }
        }
        return
    }

    val discountPct = remember {
        if (resaleItem.originalPrice > 0) {
            ((resaleItem.originalPrice - resaleItem.resalePrice) * 100) / resaleItem.originalPrice
        } else 0
    }
    val isDiscounted = resaleItem.resalePrice < resaleItem.originalPrice
    val hasInsufficientBalance = user.ctkBalance < resaleItem.resalePrice

    Scaffold(
        topBar = {
            AppHeader(
                title = "재판매 티켓",
                onBack = onBack,
                actions = {
                    TutorialHelpButton(tutorialId = TutorialId.RESALE_DETAIL)
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = Surface,
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (hasInsufficientBalance) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Danger.copy(alpha = 0.1f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = Danger, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "잔액이 부족합니다. 현재 잔액: %,d CTK".format(user.ctkBalance),
                                fontSize = 12.sp,
                                color = Danger,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Button(
                        onClick = { onPurchaseComplete(resaleItem.ticketId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Muted,
                        ),
                        enabled = !hasInsufficientBalance,
                    ) {
                        Text(
                            "구매하기",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!hasInsufficientBalance) White else MutedForeground,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Poster
            AsyncImage(
                model = resaleItem.poster,
                contentDescription = resaleItem.showName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Muted),
            )

            // Event Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        resaleItem.showName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    InfoRow(Icons.Outlined.CalendarMonth, resaleItem.showDate, tint = Primary)
                    InfoRow(Icons.Outlined.LocationOn, resaleItem.venue, tint = Primary)
                }
            }

            // Ticket Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("티켓 정보", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("좌석", fontSize = 14.sp, color = MutedForeground)
                        Text(
                            "${resaleItem.seatLabel} · ${resaleItem.grade}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnBackground,
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("정가", fontSize = 14.sp, color = MutedForeground)
                        Text(
                            "%,d CTK".format(resaleItem.originalPrice),
                            fontSize = 14.sp,
                            color = MutedForeground,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("재판매가", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isDiscounted) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(PrimaryLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                ) {
                                    Icon(
                                        Icons.Outlined.LocalOffer,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        "${discountPct}% 할인",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary,
                                    )
                                }
                            }
                            Text(
                                "%,d CTK".format(resaleItem.resalePrice),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                            )
                        }
                    }
                }
            }

            // Notice Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Muted,
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("구매 안내", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                    NoticeText("재판매 티켓은 구매 즉시 내 티켓으로 이동됩니다.")
                    NoticeText("구매가 완료되면 판매자의 리스팅은 자동으로 종료됩니다.")
                    NoticeText("잔액이 부족하면 구매가 불가하므로 충전 후 다시 시도해주세요.")
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, tint: androidx.compose.ui.graphics.Color = MutedForeground) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, fontSize = 14.sp, color = MutedForeground)
    }
}

@Composable
private fun NoticeText(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("\u2022", fontSize = 12.sp, color = MutedForeground)
        Text(text, fontSize = 12.sp, color = MutedForeground, lineHeight = 18.sp)
    }
}
