package com.example.cheketqr.presentation.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cheketqr.data.model.HostLoginRequest
import com.example.cheketqr.data.network.NetworkModule
import com.example.cheketqr.data.network.TokenStore
import kotlinx.coroutines.launch

private const val TAG = "LoginScreen"
private val Primary = Color(0xFF00C598)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Logo
            Text(
                text = "CHEKET",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
            )
            Text(
                text = "QR Scanner",
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF),
            )

            Spacer(Modifier.height(24.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("이메일") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = Color(0xFF9CA3AF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedLabelColor = Primary,
                    unfocusedLabelColor = Color(0xFF9CA3AF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            )

            // Error
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color(0xFFF87171),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "이메일과 비밀번호를 입력해주세요."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val api = NetworkModule.provideApiService()
                            val response = api.hostLogin(HostLoginRequest(email, password))
                            val body = response.body()
                            if (response.isSuccessful && body?.data != null) {
                                TokenStore.accessToken = body.data.accessToken
                                TokenStore.refreshToken = body.data.refreshToken
                                Log.d(TAG, "Login success, token=${body.data.accessToken.take(20)}...")
                                isLoading = false
                                onLoginSuccess()
                            } else {
                                isLoading = false
                                errorMessage = body?.errorMessage ?: "로그인에 실패했습니다. (${response.code()})"
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Login failed", e)
                            isLoading = false
                            errorMessage = e.message ?: "네트워크 오류가 발생했습니다."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        "로그인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Text(
                text = "주최자 계정으로 로그인하세요",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
            )
        }
    }
}
