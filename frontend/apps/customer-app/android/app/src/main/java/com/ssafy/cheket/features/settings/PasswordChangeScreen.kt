package com.ssafy.cheket.features.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.repository.AuthRepository
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PasswordChangeScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var currentError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var valid = true

        if (currentPassword.isBlank()) {
            currentError = "현재 비밀번호를 입력해주세요"
            valid = false
        } else {
            currentError = null
        }

        if (newPassword.length < 6) {
            newError = "비밀번호는 6자 이상이어야 합니다"
            valid = false
        } else {
            newError = null
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
        topBar = { AppHeader(title = "비밀번호 변경", onBack = onBack) },
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
            // Current Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("현재 비밀번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = {
                        currentPassword = it
                        currentError = null
                    },
                    placeholder = { Text("현재 비밀번호 입력", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = MutedForeground) },
                    trailingIcon = {
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(
                                if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrent) "숨기기" else "보기",
                                tint = MutedForeground,
                            )
                        }
                    },
                    visualTransformation = if (showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = currentError != null,
                    supportingText = currentError?.let { { Text(it, color = Danger) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Muted,
                        unfocusedContainerColor = Muted,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // New Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("새 비밀번호", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newError = null
                    },
                    placeholder = { Text("새 비밀번호 입력 (6자 이상)", fontSize = 14.sp) },
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
                    isError = newError != null,
                    supportingText = newError?.let { { Text(it, color = Danger) } },
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
                    if (validate() && !isLoading) {
                        isLoading = true
                        scope.launch {
                            val result = authRepository.changePassword(
                                oldPassword = currentPassword,
                                newPassword = newPassword,
                            )
                            isLoading = false
                            result.onSuccess {
                                Toast.makeText(context, "비밀번호가 변경되었습니다", Toast.LENGTH_SHORT).show()
                                onBack()
                            }.onFailure { e ->
                                currentError = e.message ?: "비밀번호 변경에 실패했습니다"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("변경하기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
