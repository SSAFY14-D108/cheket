package com.ssafy.cheket.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onFindAccount: () -> Unit = {},
    onPasswordReset: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // Logo area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Ticket icon placeholder
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Primary,
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("C", fontSize = 32.sp, fontWeight = FontWeight.Black, color = White)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "로그인",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Primary,
                letterSpacing = (-1).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text("cheket 계정으로 티켓 서비스를 이용하세요", fontSize = 14.sp, color = MutedForeground)
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ID field
            OutlinedTextField(
                value = uiState.id,
                onValueChange = viewModel::onIdChange,
                placeholder = { Text("아이디", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = MutedForeground) },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Muted,
                    unfocusedContainerColor = Muted,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Password field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("비밀번호", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = MutedForeground) },
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            if (uiState.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (uiState.showPassword) "비밀번호 숨기기" else "비밀번호 보기",
                            tint = MutedForeground,
                        )
                    }
                },
                visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Muted,
                    unfocusedContainerColor = Muted,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Error message
            if (uiState.error.isNotEmpty()) {
                Text(
                    uiState.error,
                    fontSize = 12.sp,
                    color = Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Login button
            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("로그인", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
            }

            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f), color = BorderColor)
                Text(
                    "또는",
                    fontSize = 12.sp,
                    color = MutedForeground,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                HorizontalDivider(Modifier.weight(1f), color = BorderColor)
            }

            // Sign up button
            OutlinedButton(
                onClick = onNavigateToSignup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Muted),
                border = ButtonDefaults.outlinedButtonBorder(true),
            ) {
                Text("회원가입", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
            }

            // Help links
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    "이메일 찾기",
                    fontSize = 12.sp,
                    color = Primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onFindAccount() },
                )
                Text(" · ", fontSize = 12.sp, color = MutedForeground)
                Text(
                    "비밀번호 찾기",
                    fontSize = 12.sp,
                    color = Primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onPasswordReset() },
                )
            }

            // Footer
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp),
                text = buildAnnotatedString {
                    append("로그인 시 ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("이용약관")
                    }
                    append(" 및 ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("개인정보처리방침")
                    }
                    append("에 동의합니다")
                },
                fontSize = 11.sp,
                color = MutedForeground,
                lineHeight = 16.sp,
            )
        }
    }
}
