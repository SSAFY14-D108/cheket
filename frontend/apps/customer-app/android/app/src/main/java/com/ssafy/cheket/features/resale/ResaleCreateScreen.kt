package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.CardBg
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryDark
import com.ssafy.cheket.ui.theme.PrimaryLight
import com.ssafy.cheket.ui.theme.SubText
import com.ssafy.cheket.ui.theme.Warning
import com.ssafy.cheket.ui.theme.White

@Composable
fun ResaleCreateScreen(
    ticketId: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ResaleCreateViewModel = viewModel(factory = ResaleCreateViewModel.factory(ticketId)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ticket = uiState.ticket
    var priceInput by rememberSaveable { mutableStateOf("") }

    val price = priceInput.toIntOrNull()
    val isOverPrice = ticket != null && price != null && price > ticket.originalPrice
    val isValidPrice = ticket != null && price != null && price > 0 && price <= ticket.originalPrice
    val discountPct = if (ticket != null && isValidPrice) {
        ((ticket.originalPrice - price!!) * 100) / ticket.originalPrice
    } else {
        0
    }

    if (ticket != null && uiState.submitMessage != null) {
        ResaleCreateSubmittedScreen(
            ticket = ticket,
            submittedPrice = price ?: 0,
            submitMessage = uiState.submitMessage ?: "2차 판매 등록 요청이 접수되었습니다.",
            transactionId = uiState.transactionId,
            onSuccess = onSuccess,
        )
        return
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "2차 판매 등록",
                onBack = onBack,
                actions = {
                    TutorialHelpButton(tutorialId = TutorialId.RESALE_CREATE)
                },
            )
        },
        bottomBar = {
            if (ticket != null && !uiState.isLoadingTicket) {
                Surface(tonalElevation = 4.dp, shadowElevation = 8.dp) {
                    Column(Modifier.padding(16.dp)) {
                        Button(
                            onClick = { price?.let(viewModel::submitResale) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = Muted,
                            ),
                            enabled = isValidPrice && !uiState.isSubmitting,
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = White,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.size(8.dp))
                                Text("등록 요청 중...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("2차 판매 요청", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoadingTicket -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            ticket == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "티켓 정보를 불러오지 못했습니다.",
                        color = MutedForeground,
                        fontSize = 14.sp,
                    )
                }
            }

            else -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ResaleTicketSummaryCard(ticket = ticket)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("판매 가격 설정", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                            OutlinedTextField(
                                value = priceInput,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() }) {
                                        priceInput = newValue
                                        viewModel.clearError()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("가격을 입력해주세요", color = SubText) },
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
                                InlineWarningCard(
                                    text = "정가(${ticket.originalPrice.formatCtk()})를 초과해 등록할 수 없습니다.",
                                )
                            }

                            if (isValidPrice && discountPct > 0) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = PrimaryLight),
                                ) {
                                    Text(
                                        text = "정가 대비 ${discountPct}% 할인된 가격입니다.",
                                        fontSize = 12.sp,
                                        color = PrimaryDark,
                                        modifier = Modifier.padding(12.dp),
                                    )
                                }
                            }

                            uiState.errorMessage?.let { message ->
                                InlineWarningCard(text = message)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryLight),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Text("안내사항", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryDark)
                            }
                            NoticeText("2차 판매 가격은 정가 이하로만 설정할 수 있습니다.")
                            NoticeText("요청 접수 후 블록체인 처리까지는 약간의 시간이 걸릴 수 있습니다.")
                            NoticeText("등록이 완료되면 내 티켓 목록에서 판매중 상태로 표시됩니다.")
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ResaleCreateSubmittedScreen(
    ticket: Ticket,
    submittedPrice: Int,
    submitMessage: String,
    transactionId: Long?,
    onSuccess: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppHeader(
                title = "2차 판매 요청 완료",
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
            Text(submitMessage, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            Spacer(Modifier.height(8.dp))
            Text(
                "블록체인 처리 후 2차 거래소에 티켓이 노출됩니다.",
                fontSize = 14.sp,
                color = MutedForeground,
            )
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
                        "등록 가격 ${submittedPrice.formatCtk()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    transactionId?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("TX ID #$it", fontSize = 12.sp, color = MutedForeground)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("내 티켓으로 돌아가기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
            }
        }
    }
}

@Composable
private fun ResaleTicketSummaryCard(ticket: Ticket) {
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
            Spacer(Modifier.size(12.dp))
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
                    "정가 ${ticket.originalPrice.formatCtk()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            }
        }
    }
}

@Composable
private fun InlineWarningCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
            Text(text, fontSize = 12.sp, color = Warning)
        }
    }
}

@Composable
private fun CreateInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = MutedForeground)
    }
}

@Composable
private fun NoticeText(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("\u2022", fontSize = 12.sp, color = PrimaryDark)
        Text(text, fontSize = 12.sp, color = PrimaryDark, lineHeight = 18.sp)
    }
}

private fun Int.formatCtk(): String = "%,d CTK".format(this)
