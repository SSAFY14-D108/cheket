package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.input.KeyboardType
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
fun ResaleCreateScreen(
    ticketId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val ticket = remember { MockDataSource.mockTickets.find { it.id == ticketId } }
    var priceInput by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    if (ticket == null) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "양도 등록",
                    onBack = onBack,
                    actions = {
                        TutorialHelpButton(tutorialId = TutorialId.RESALE_CREATE)
                    },
                )
            },
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("티켓을 찾을 수 없습니다.", color = MutedForeground, fontSize = 14.sp)
            }
        }
        return
    }

    val price = priceInput.toIntOrNull()
    val isOverPrice = price != null && price > ticket.originalPrice
    val isValidPrice = price != null && price > 0 && price <= ticket.originalPrice
    val discountPct = if (isValidPrice && ticket.originalPrice > 0) {
        ((ticket.originalPrice - price!!) * 100) / ticket.originalPrice
    } else 0

    // Success State
    if (isSubmitted) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "양도 등록 완료",
                    actions = {
                        TutorialHelpButton(tutorialId = TutorialId.RESALE_CREATE)
                    },
                )
            },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(56.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text("양도 등록이 완료되었습니다!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                Spacer(Modifier.height(8.dp))
                Text("리세일 마켓에 티켓이 등록되었습니다.", fontSize = 14.sp, color = MutedForeground)
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ticket.showName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                        Spacer(Modifier.height(4.dp))
                        Text("${ticket.seatLabel} (${ticket.grade})", fontSize = 13.sp, color = MutedForeground)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "등록 가격: %,d CTK".format(price ?: 0),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onSuccess,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                ) {
                    Text("내 티켓 보기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }
        }
        return
    }

    // Create Form
    Scaffold(
        topBar = {
            AppHeader(
                title = "양도 등록",
                onBack = onBack,
                actions = {
                    TutorialHelpButton(tutorialId = TutorialId.RESALE_CREATE)
                },
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 8.dp, color = Surface) {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { isSubmitted = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Muted,
                        ),
                        enabled = isValidPrice,
                    ) {
                        Text(
                            "양도 등록",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValidPrice) White else MutedForeground,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ticket Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ticket.poster,
                        contentDescription = ticket.showName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 70.dp, height = 94.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Muted),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            ticket.showName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        CreateInfoRow(Icons.Outlined.CalendarMonth, ticket.showDate)
                        CreateInfoRow(Icons.Outlined.LocationOn, ticket.venue)
                        CreateInfoRow(Icons.Outlined.EventSeat, "${ticket.seatLabel} (${ticket.grade})")
                        Text(
                            "정가: %,d원".format(ticket.originalPrice),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                        )
                    }
                }
            }

            // Price Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("양도 가격 설정", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) priceInput = newValue
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("가격을 입력하세요", color = SubText) },
                        suffix = { Text("CTK", fontWeight = FontWeight.Bold, color = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Primary,
                        ),
                    )

                    if (isOverPrice) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Warning.copy(alpha = 0.1f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "정가(%,d원)를 초과할 수 없습니다.".format(ticket.originalPrice),
                                fontSize = 12.sp,
                                color = Warning,
                            )
                        }
                    }

                    if (isValidPrice && discountPct > 0) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryLight)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "정가 대비 ${discountPct}% 할인 가격입니다.",
                                fontSize = 12.sp,
                                color = PrimaryDark,
                            )
                        }
                    }
                }
            }

            // Notice Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLight),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Text("양도 등록 안내", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryDark)
                    }
                    CreateNoticeText("양도 가격은 정가 이하로만 설정할 수 있습니다.")
                    CreateNoticeText("등록 후 구매자가 나타나면 자동으로 양도가 진행됩니다.")
                    CreateNoticeText("양도 완료 전까지 취소가 가능합니다.")
                    CreateNoticeText("양도 대금은 완료 즉시 지갑으로 입금됩니다.")
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CreateInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = MutedForeground)
    }
}

@Composable
private fun CreateNoticeText(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("\u2022", fontSize = 12.sp, color = PrimaryDark)
        Text(text, fontSize = 12.sp, color = PrimaryDark, lineHeight = 18.sp)
    }
}
