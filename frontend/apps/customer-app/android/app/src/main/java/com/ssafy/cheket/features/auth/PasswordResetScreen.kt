package com.ssafy.cheket.features.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun PasswordResetScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var emailOrPhone by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var codeSent by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    fun sendCode() {
        if (emailOrPhone.isBlank()) {
            emailError = "이메일 또는 전화번호를 입력해주세요"
            return
        }
        emailError = null
        codeSent = true
        Toast.makeText(context, "인증 코드가 발송되었습니다", Toast.LENGTH_SHORT).show()
    }

    fun validate(): Boolean {
        var valid = true

        if (emailOrPhone.isBlank()) {
            emailError = "이메일 또는 전화번호를 입력해주세요"
            valid = false
        } else {
            emailError = null
        }

        if (smsCode.isBlank() || smsCode.length < 4) {
            codeError = "인증 코드를 입력해주세요"
            valid = false
        } else {
            codeError = null
        }

        if (newPassword.length < 6) {
            passwordError = "비밀번호는 6자 이상이어야 합니다"
            valid = false
        } else {
            passwordError = null
        }

        if (confirmPassword != newPassword) {
            confirmError = "비밀번호가 일치하지 않습니다"
            valid = false
        } else if (confirmPassword.isBlank()) {
            confirmError = "비밀번호 확인을 입력해주세요"
            valid = false
        } else {
            confirmError = null
        }

        return valid
    }

    Scaffold(
        topBar = { AppHeader(title = "비밀번호 재설정", onBack = onBack) },
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
                "가입 시 등록한 이메일 또는 전화번호를 입력하면 인증 코드를 발송합니다.",
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp,
            )

            // Email / Phone Input with Send Code button
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("이메일 / 전화번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.Top,
                ) {
                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = {
                            emailOrPhone = it
                            emailError = null
                        },
                        placeholder = { Text("이메일 또는 전화번호", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MutedForeground) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = Danger) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Muted,
                            unfocusedContainerColor = Muted,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { sendCode() },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (codeSent) MutedForeground else Primary,
                        ),
                    ) {
                        Text(
                            if (codeSent) "재발송" else "발송",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White,
                        )
                    }
                }
            }

            // SMS Code Input
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("인증 코드", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = {
                        smsCode = it.filter { c -> c.isDigit() }
                        codeError = null
                    },
                    placeholder = { Text("인증 코드 입력", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Sms, null, tint = MutedForeground) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = codeError != null,
                    supportingText = codeError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HorizontalDivider(color = BorderColor)

            // New Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("새 비밀번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null
                    },
                    placeholder = { Text("새 비밀번호 (6자 이상)", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MutedForeground) },
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNew) "숨기기" else "보기",
                                tint = MutedForeground,
                            )
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = passwordError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Confirm Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("비밀번호 확인", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmError = null
                    },
                    placeholder = { Text("새 비밀번호 재입력", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MutedForeground) },
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirm) "숨기기" else "보기",
                                tint = MutedForeground,
                            )
                        }
                    },
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = confirmError != null,
                    supportingText = confirmError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    if (validate()) {
                        Toast.makeText(context, "비밀번호가 재설정되었습니다", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("재설정", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
