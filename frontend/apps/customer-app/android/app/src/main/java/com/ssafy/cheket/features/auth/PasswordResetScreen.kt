package com.ssafy.cheket.features.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.network.dto.PasswordChangeRequest
import com.ssafy.cheket.core.network.dto.PasswordResetRequest
import com.ssafy.cheket.core.network.service.AuthService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.PhoneVisualTransformation
import com.ssafy.cheket.core.ui.component.formatPhoneForApi
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.launch

private const val TAG = "PasswordResetScreen"
private val ResetInputBackground = Color(0xFFF6F7F8)
private val ResetInputIconTint = Color(0xFF7A8682)
private val ResetInputBorder = Color(0xFFE1E5E8)
private val ResetFieldShape = RoundedCornerShape(22.dp)

@Composable
fun PasswordResetScreen(
    authService: AuthService,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var codeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    fun sendCode() {
        if (email.isBlank() || !email.contains("@")) {
            emailError = "이메일을 입력해 주세요"
            return
        }
        emailError = null
        isLoading = true
        scope.launch {
            try {
                val response = authService.requestPasswordReset(PasswordResetRequest(email))
                Log.d(TAG, "requestPasswordReset() statusCode=${response.httpStatusCode}")
                isLoading = false
                if (response.httpStatusCode in 200..299) {
                    codeSent = true
                    Toast.makeText(context, "인증번호를 전송했어요", Toast.LENGTH_SHORT).show()
                } else {
                    emailError = response.responseMessage ?: "인증번호 전송에 실패했어요"
                }
            } catch (e: Exception) {
                Log.e(TAG, "requestPasswordReset() error", e)
                isLoading = false
                emailError = "인증번호 전송에 실패했어요"
            }
        }
    }

    fun validate(): Boolean {
        var valid = true

        if (email.isBlank()) {
            emailError = "이메일을 입력해 주세요"
            valid = false
        } else emailError = null

        if (phone.isBlank() || phone.length < 10) {
            phoneError = "전화번호를 입력해 주세요"
            valid = false
        } else phoneError = null

        if (smsCode.isBlank() || smsCode.length < 4) {
            codeError = "인증번호를 입력해 주세요"
            valid = false
        } else codeError = null

        if (newPassword.length < 6) {
            passwordError = "비밀번호는 6자 이상이어야 해요"
            valid = false
        } else passwordError = null

        if (confirmPassword.isBlank()) {
            confirmError = "비밀번호 확인을 입력해 주세요"
            valid = false
        } else if (confirmPassword != newPassword) {
            confirmError = "비밀번호가 일치하지 않아요"
            valid = false
        } else confirmError = null

        return valid
    }

    fun submit() {
        if (!validate() || isLoading) return
        isLoading = true
        scope.launch {
            try {
                val response = authService.resetPassword(
                    PasswordChangeRequest(
                        phoneNumber = formatPhoneForApi(phone),
                        code = smsCode,
                        newPassword = newPassword,
                    ),
                )
                Log.d(TAG, "resetPassword() statusCode=${response.httpStatusCode}")
                isLoading = false
                if (response.httpStatusCode in 200..299) {
                    Toast.makeText(context, "비밀번호가 변경되었어요", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    codeError = response.responseMessage ?: "비밀번호 재설정에 실패했어요"
                }
            } catch (e: Exception) {
                Log.e(TAG, "resetPassword() error", e)
                isLoading = false
                codeError = "비밀번호 재설정에 실패했어요"
            }
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "비밀번호 재설정", onBack = onBack) },
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
                text = "등록된 이메일로 인증번호를 받고 새 비밀번호를 설정해 주세요.",
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp,
            )

            ResetInputBlock("이메일", emailError) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        placeholder = { Text("이메일을 입력해 주세요", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = ResetInputIconTint) },
                        singleLine = true,
                        shape = ResetFieldShape,
                        colors = resetInputColors(),
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = ::sendCode,
                        enabled = !isLoading,
                        modifier = Modifier
                            .height(52.dp)
                            .gradientBorder(shape = ResetFieldShape, borderWidth = 1.5.dp),
                        shape = ResetFieldShape,
                        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = OnBackground),
                    ) {
                        Text(
                            text = if (codeSent) "재전송" else "전송",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }
                }
            }

            ResetInputBlock("전화번호", phoneError) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter(Char::isDigit).take(11)
                        phoneError = null
                    },
                    placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = ResetInputIconTint) },
                    visualTransformation = PhoneVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = ResetFieldShape,
                    colors = resetInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ResetInputBlock("인증번호", codeError) {
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = {
                        smsCode = it.filter(Char::isDigit)
                        codeError = null
                    },
                    placeholder = { Text("인증번호를 입력해 주세요", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Sms, null, tint = ResetInputIconTint) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = ResetFieldShape,
                    colors = resetInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            HorizontalDivider(color = ResetInputBorder)

            ResetInputBlock("새 비밀번호", passwordError) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null
                    },
                    placeholder = { Text("새 비밀번호 (6자 이상)", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ResetInputIconTint) },
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                imageVector = if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNew) "숨기기" else "보기",
                                tint = ResetInputIconTint,
                            )
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = ResetFieldShape,
                    colors = resetInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ResetInputBlock("비밀번호 확인", confirmError) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmError = null
                    },
                    placeholder = { Text("비밀번호를 다시 입력해 주세요", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = ResetInputIconTint) },
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirm) "숨기기" else "보기",
                                tint = ResetInputIconTint,
                            )
                        }
                    },
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = ResetFieldShape,
                    colors = resetInputColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = ::submit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .gradientBorder(shape = ResetFieldShape, borderWidth = 1.5.dp),
                shape = ResetFieldShape,
                colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = OnBackground),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnBackground, strokeWidth = 2.dp)
                } else {
                    Text("변경하기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ResetInputBlock(
    label: String,
    error: String?,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
        content()
        if (!error.isNullOrBlank()) {
            Text(text = error, fontSize = 12.sp, color = Danger)
        }
    }
}

@Composable
private fun resetInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ResetInputBorder,
    unfocusedBorderColor = ResetInputBorder,
    focusedContainerColor = ResetInputBackground,
    unfocusedContainerColor = ResetInputBackground,
)
