package com.ssafy.cheket.features.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.network.dto.EmailFindRequest
import com.ssafy.cheket.core.network.service.AuthService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

private const val TAG = "FindAccountScreen"

@Composable
fun FindAccountScreen(
    authService: AuthService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var foundEmail by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun onSubmit() {
        var valid = true
        if (name.isBlank()) {
            nameError = "이름을 입력해주세요"
            valid = false
        } else {
            nameError = null
        }
        if (phone.isBlank() || phone.length < 10) {
            phoneError = "올바른 전화번호를 입력해주세요"
            valid = false
        } else {
            phoneError = null
        }
        if (!valid) return

        isLoading = true
        generalError = null
        scope.launch {
            try {
                val response = authService.findEmail(EmailFindRequest(username = name, phoneNumber = com.ssafy.cheket.core.ui.component.formatPhoneForApi(phone)))
                Log.d(TAG, "findEmail() statusCode=${response.httpStatusCode}")
                isLoading = false
                if (response.httpStatusCode in 200..299 && response.data != null) {
                    foundEmail = response.data.email
                } else {
                    generalError = response.responseMessage ?: "계정을 찾을 수 없습니다"
                }
            } catch (e: Exception) {
                Log.e(TAG, "findEmail() error", e)
                isLoading = false
                generalError = "계정 찾기에 실패했습니다"
            }
        }
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
            Text(
                "가입 시 등록한 이름과 전화번호를 입력하시면 연결된 계정 정보를 확인할 수 있습니다.",
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp,
            )

            // Name Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("이름", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                        foundEmail = null
                        generalError = null
                    },
                    placeholder = { Text("이름 입력", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MutedForeground) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Phone Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("전화번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter { c -> c.isDigit() }.take(11)
                        phoneError = null
                        foundEmail = null
                        generalError = null
                    },
                    placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = MutedForeground) },
                    visualTransformation = com.ssafy.cheket.core.ui.component.PhoneVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            Button(
                onClick = { onSubmit() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("확인", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
                }
            }

            if (generalError != null) {
                Text(generalError!!, color = Danger, fontSize = 13.sp)
            }

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
                        Icon(Icons.Outlined.CheckCircle, null, tint = Success, modifier = Modifier.size(24.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("계정을 찾았습니다", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Success)
                            Text("연결된 이메일: $foundEmail", fontSize = 13.sp, color = OnBackground)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
