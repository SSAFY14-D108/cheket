package com.ssafy.cheket.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun FindAccountScreen(
    onBack: () -> Unit,
) {
    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var foundEmail by remember { mutableStateOf<String?>(null) }

    fun onSubmit() {
        if (phone.isBlank() || phone.length < 10) {
            phoneError = "올바른 전화번호를 입력해주세요"
            return
        }
        phoneError = null
        // Mock: mask an email based on the phone
        foundEmail = "k***n@email.com"
    }

    Scaffold(
        topBar = { AppHeader(title = "계정 찾기", onBack = onBack) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Description
            Text(
                "가입 시 등록한 전화번호를 입력하시면 연결된 계정 정보를 확인할 수 있습니다.",
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp,
            )

            // Phone Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("전화번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter { c -> c.isDigit() || c == '-' }
                        phoneError = null
                        foundEmail = null
                    },
                    placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = MutedForeground) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Submit Button
            Button(
                onClick = { onSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("확인", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }

            // Result Display
            if (foundEmail != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.08f)),
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(24.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "계정을 찾았습니다",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Success,
                            )
                            Text(
                                "연결된 이메일: $foundEmail",
                                fontSize = 13.sp,
                                color = OnBackground,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
