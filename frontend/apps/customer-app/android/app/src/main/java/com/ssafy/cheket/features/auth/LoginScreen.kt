package com.ssafy.cheket.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
import com.ssafy.cheket.core.ui.component.gradientBorder
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
            .verticalScroll(rememberScrollState()),
    ) {
        // Gradient header with wave decoration (matching v0 design)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xF2E2DAFF),
                                0.52f to Color(0xEBC4F7E0),
                                1.0f to Color(0xF2CAE6FF),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        ),
                    ),
            )
            // Wave curve at bottom
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .align(Alignment.BottomCenter),
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.73f) // ~102/140
                    cubicTo(
                        w * 0.236f, h * 0.386f, // C1: 92/390, 54/140
                        w * 0.764f, h * 0.386f, // C2: 298/390, 54/140
                        w, h * 0.73f,            // end: 390, 102
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color = Background)
            }
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title heading (matching v0)
            Text(
                "로그인",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                letterSpacing = (-1.12).sp,
            )

            Spacer(Modifier.height(16.dp))

            // ID field (bottom-border style)
            BasicTextField(
                value = uiState.id,
                onValueChange = viewModel::onIdChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = OnBackground),
                cursorBrush = SolidColor(Primary),
                decorationBox = { innerTextField ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Person, null,
                                tint = Color(0xFF5C7A73),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(Modifier.weight(1f)) {
                                if (uiState.id.isEmpty()) {
                                    Text("아이디", fontSize = 14.sp, color = Color(0xFF7F9891))
                                }
                                innerTextField()
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFD8EFEA)),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            // Password field (bottom-border style)
            BasicTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = OnBackground),
                cursorBrush = SolidColor(Primary),
                visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                decorationBox = { innerTextField ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Lock, null,
                                tint = Color(0xFF5C7A73),
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(Modifier.weight(1f)) {
                                if (uiState.password.isEmpty()) {
                                    Text("비밀번호", fontSize = 14.sp, color = Color(0xFF7F9891))
                                }
                                innerTextField()
                            }
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    if (uiState.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (uiState.showPassword) "비밀번호 숨기기" else "비밀번호 보기",
                                    tint = Color(0xFF5C7A73),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFD8EFEA)),
                        )
                    }
                },
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

            // Login button (rounded-full with gradient border like v0)
            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .gradientBorder(shape = RoundedCornerShape(50), borderWidth = 1.5.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("로그인", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = White)
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

            // Sign up button (outline with gradient border)
            OutlinedButton(
                onClick = onNavigateToSignup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .gradientBorder(shape = RoundedCornerShape(50), borderWidth = 1.5.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                border = BorderStroke(0.dp, Color.Transparent),
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
