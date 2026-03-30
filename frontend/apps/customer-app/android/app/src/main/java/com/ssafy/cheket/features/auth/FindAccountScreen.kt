package com.ssafy.cheket.features.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.network.dto.EmailFindRequest
import com.ssafy.cheket.core.network.service.AuthService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.PhoneVisualTransformation
import com.ssafy.cheket.core.ui.component.formatPhoneForApi
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Success
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.launch

private const val TAG = "FindAccountScreen"
private val FindInputBackground = Color(0xFFF6F7F8)
private val FindInputIconTint = Color(0xFF7A8682)
private val FindInputBorder = Color(0xFFE1E5E8)
private val FindFieldShape = RoundedCornerShape(22.dp)

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

    fun submit() {
        var valid = true
        if (name.isBlank()) {
            nameError = "이름을 입력해 주세요"
            valid = false
        } else {
            nameError = null
        }
        if (phone.isBlank() || phone.length < 10) {
            phoneError = "전화번호를 입력해 주세요"
            valid = false
        } else {
            phoneError = null
        }
        if (!valid) return

        isLoading = true
        generalError = null
        foundEmail = null

        scope.launch {
            try {
                val response = authService.findEmail(
                    EmailFindRequest(
                        username = name,
                        phoneNumber = formatPhoneForApi(phone),
                    ),
                )
                Log.d(TAG, "findEmail() statusCode=${response.httpStatusCode}")
                isLoading = false
                if (response.httpStatusCode in 200..299 && response.data != null) {
                    foundEmail = response.data.email
                } else {
                    generalError = response.responseMessage ?: "아이디를 찾지 못했어요"
                }
            } catch (e: Exception) {
                Log.e(TAG, "findEmail() error", e)
                isLoading = false
                generalError = "아이디 찾기에 실패했어요"
            }
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "아이디 찾기", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "회원가입에 사용한 이름과 전화번호를 입력하면 등록된 이메일을 확인할 수 있어요.",
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("이름", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                        generalError = null
                        foundEmail = null
                    },
                    placeholder = { Text("이름을 입력해 주세요", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = FindInputIconTint) },
                    singleLine = true,
                    shape = FindFieldShape,
                    colors = findAccountInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                nameError?.let { Text(it, fontSize = 12.sp, color = Danger) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("전화번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter(Char::isDigit).take(11)
                        phoneError = null
                        generalError = null
                        foundEmail = null
                    },
                    placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = FindInputIconTint) },
                    visualTransformation = PhoneVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = FindFieldShape,
                    colors = findAccountInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                phoneError?.let { Text(it, fontSize = 12.sp, color = Danger) }
            }

            Button(
                onClick = ::submit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .gradientBorder(shape = FindFieldShape, borderWidth = 1.5.dp),
                shape = FindFieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = OnBackground),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = OnBackground,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("확인", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                }
            }

            generalError?.let { Text(it, color = Danger, fontSize = 13.sp) }

            foundEmail?.let { email ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FindFieldShape,
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.08f)),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Success)
                        Text("아이디를 찾았어요", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Success)
                        Text("등록된 이메일: $email", fontSize = 13.sp, color = OnBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun findAccountInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FindInputBorder,
    unfocusedBorderColor = FindInputBorder,
    focusedContainerColor = FindInputBackground,
    unfocusedContainerColor = FindInputBackground,
)
