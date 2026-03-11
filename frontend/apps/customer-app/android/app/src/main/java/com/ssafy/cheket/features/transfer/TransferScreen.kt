package com.ssafy.cheket.features.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun TransferScreen(
    ticketId: String,
    onTransferComplete: (String) -> Unit,
    onTransferFailed: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TransferViewModel = viewModel(factory = TransferViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val ticket = remember(ticketId) {
        MockDataSource.mockTickets.find { it.id == ticketId }
    }

    Scaffold(
        topBar = { AppHeader(title = "티켓 양도", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // --- Ticket Summary Card ---
            if (ticket != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = ticket.poster,
                            contentDescription = ticket.showName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(56.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                ticket.showName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                ticket.showDate,
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                            Text(
                                "${ticket.seatLabel} / ${ticket.grade}",
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }

            // --- Phone Input Section ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "양도 대상자 확인",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )

                    OutlinedTextField(
                        value = uiState.formattedPhone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("010-0000-0000", color = SubText, fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.verifyRecipient()
                            },
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Primary,
                        ),
                        isError = uiState.verifyError != null,
                        supportingText = if (uiState.verifyError != null) {
                            { Text(uiState.verifyError!!, color = Danger, fontSize = 12.sp) }
                        } else null,
                    )

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.verifyRecipient()
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        enabled = uiState.phone.length >= 10 && !uiState.isVerifying,
                    ) {
                        if (uiState.isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("확인", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // --- Verified User Card ---
            if (uiState.verifiedName != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Success.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                uiState.verifiedName!!,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground,
                            )
                            Text(
                                uiState.formattedPhone,
                                fontSize = 13.sp,
                                color = MutedForeground,
                            )
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "확인됨",
                            tint = Success,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // --- Notice Section ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.10f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Warning.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "유의사항",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }
                    NoticeItem("양도 후에는 티켓 소유권이 즉시 이전됩니다.")
                    NoticeItem("양도된 티켓은 되돌릴 수 없습니다.")
                    NoticeItem("블록체인 네트워크 상태에 따라 처리 시간이 소요될 수 있습니다.")
                    NoticeItem("양도 대상자가 Cheket 회원이어야 합니다.")
                }
            }

            // --- Transfer Button ---
            Button(
                onClick = {
                    // Save to NavParams before navigating
                    NavParams.recipientName = uiState.verifiedName ?: ""
                    NavParams.recipientPhone = uiState.formattedPhone

                    viewModel.submitTransfer(
                        ticketId = ticketId,
                        onSuccess = { id ->
                            onTransferComplete(id)
                        },
                        onFailure = { id ->
                            NavParams.transferFailureReason = "블록체인 트랜잭션 처리 중 오류가 발생했습니다. 네트워크 상태를 확인 후 다시 시도해주세요."
                            onTransferFailed(id)
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = uiState.verifiedName != null && !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("양도 처리 중...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Text("양도하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NoticeItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "\u2022",
            fontSize = 12.sp,
            color = MutedForeground,
            modifier = Modifier.padding(end = 6.dp, top = 1.dp),
        )
        Text(
            text,
            fontSize = 12.sp,
            color = MutedForeground,
            lineHeight = 18.sp,
        )
    }
}
