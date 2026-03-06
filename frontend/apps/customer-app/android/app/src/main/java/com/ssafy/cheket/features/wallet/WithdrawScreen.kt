package com.ssafy.cheket.features.wallet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WithdrawScreen(
    onBack: () -> Unit,
) {
    val user = remember { MockDataSource.mockUser }
    val balance = remember { user.ctkBalance }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toIntOrNull() ?: 0

    fun validate(): Boolean {
        var valid = true
        if (amountText.isBlank() || amount <= 0) {
            amountError = "출금 금액을 입력해주세요"
            valid = false
        } else if (amount > balance) {
            amountError = "잔액을 초과할 수 없습니다"
            valid = false
        } else {
            amountError = null
        }

        if (address.isBlank()) {
            addressError = "출금 주소를 입력해주세요"
            valid = false
        } else if (!address.startsWith("0x") || address.length < 10) {
            addressError = "올바른 지갑 주소를 입력해주세요"
            valid = false
        } else {
            addressError = null
        }

        return valid
    }

    Scaffold(
        topBar = { AppHeader(title = "출금", onBack = onBack) },
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
            // Current Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Column {
                            Text("현재 잔액", fontSize = 12.sp, color = SubText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${numberFormat.format(balance)} CTK",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                            )
                        }
                    }
                }
            }

            // Amount Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("출금 금액", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { c -> c.isDigit() }
                        amountError = null
                    },
                    placeholder = { Text("금액을 입력하세요", fontSize = 14.sp) },
                    suffix = { Text("CTK", fontSize = 14.sp, color = MutedForeground) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Address Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("출금 주소", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        addressError = null
                    },
                    placeholder = { Text("0x...", fontSize = 14.sp, fontFamily = FontFamily.Monospace) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = addressError != null,
                    supportingText = addressError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Withdraw Button
            Button(
                onClick = {
                    if (validate()) {
                        Toast.makeText(context, "출금 요청이 접수되었습니다", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("출금하기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }

            // Info Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.08f)),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Info,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "출금 안내",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Info,
                        )
                        Text(
                            "출금 요청 후 블록체인 네트워크 확인까지 약 10~30분이 소요될 수 있습니다. 출금 완료 시 알림으로 안내드립니다.",
                            fontSize = 12.sp,
                            color = MutedForeground,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
