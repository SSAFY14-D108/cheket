package com.ssafy.cheket.features.transfer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.CardBg
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.SubText
import com.ssafy.cheket.ui.theme.Warning
import com.ssafy.cheket.ui.theme.White

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
        NavParams.selectedTicket?.takeIf { it.id == ticketId }
            ?: MockDataSource.mockTickets.find { it.id == ticketId }
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
            if (ticket != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
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
                                text = ticket.showName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(text = ticket.showDate, fontSize = 12.sp, color = MutedForeground)
                            Text(
                                text = "${ticket.seatLabel} / ${ticket.grade}",
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "받는 사람 전화번호",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )

                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("010-1234-5678", color = SubText, fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Primary,
                        ),
                        visualTransformation = PhoneNumberVisualTransformation(),
                        isError = uiState.phoneError != null,
                        supportingText = uiState.phoneError?.let { message ->
                            { Text(message, color = Danger, fontSize = 12.sp) }
                        },
                    )

                    Text(
                        text = "숫자만 입력하면 양도 요청 시 자동으로 형식이 적용됩니다.",
                        fontSize = 12.sp,
                        color = MutedForeground,
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.10f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Warning.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "안내사항",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }
                    NoticeItem("양도 요청 후에는 티켓이 즉시 이전될 수 있어요.")
                    NoticeItem("받는 사람 전화번호를 다시 한 번 확인해주세요.")
                    NoticeItem("양도 실패 시 실패 화면에서 사유를 확인할 수 있어요.")
                }
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    NavParams.recipientName = ""
                    NavParams.recipientPhone = uiState.formattedPhone

                    viewModel.submitTransfer(
                        ticketId = ticketId,
                        onSuccess = { id ->
                            NavParams.transferFailureReason = ""
                            onTransferComplete(id)
                        },
                        onFailure = { id, message ->
                            NavParams.transferFailureReason = message
                            onTransferFailed(id)
                        },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !uiState.isSubmitting,
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("양도 요청 중...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Text("양도하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                append(char)
                if (index == 2 && digits.length > 3) append('-')
                if (index == 6 && digits.length > 7) append('-')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 7 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(formatted.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 8 -> offset - 1
                else -> offset - 2
            }.coerceIn(0, digits.length)
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
private fun NoticeItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = "\u2022",
            fontSize = 12.sp,
            color = MutedForeground,
            modifier = Modifier.padding(end = 6.dp, top = 1.dp),
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MutedForeground,
            lineHeight = 18.sp,
        )
    }
}
