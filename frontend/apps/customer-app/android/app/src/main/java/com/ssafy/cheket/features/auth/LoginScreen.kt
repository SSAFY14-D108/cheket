package com.ssafy.cheket.features.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.White

private val LoginDividerColor = Color(0xFFE1E5E8)
private val LoginIconTint = Color(0xFF6E7B78)
private val LoginPlaceholderTint = Color(0xFF8A9491)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onFindAccount: () -> Unit = {},
    onPasswordReset: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
        ) {
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

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(0f, h * 0.73f)
                    cubicTo(
                        w * 0.236f, h * 0.386f,
                        w * 0.764f, h * 0.386f,
                        w, h * 0.73f,
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color = Background)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "로그인",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                letterSpacing = (-1.12).sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = uiState.id,
                onValueChange = viewModel::onIdChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = OnBackground),
                cursorBrush = SolidColor(OnBackground),
                decorationBox = { innerTextField ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LoginIconTint,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.id.isEmpty()) {
                                    Text(
                                        text = "이메일",
                                        fontSize = 14.sp,
                                        color = LoginPlaceholderTint,
                                    )
                                }
                                innerTextField()
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LoginDividerColor),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            BasicTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = OnBackground),
                cursorBrush = SolidColor(OnBackground),
                visualTransformation = if (uiState.showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                decorationBox = { innerTextField ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = LoginIconTint,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.password.isEmpty()) {
                                    Text(
                                        text = "비밀번호",
                                        fontSize = 14.sp,
                                        color = LoginPlaceholderTint,
                                    )
                                }
                                innerTextField()
                            }
                            IconButton(
                                onClick = viewModel::togglePasswordVisibility,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = if (uiState.showPassword) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = if (uiState.showPassword) {
                                        "비밀번호 숨기기"
                                    } else {
                                        "비밀번호 보기"
                                    },
                                    tint = LoginIconTint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LoginDividerColor),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.error.isNotEmpty()) {
                Text(
                    text = uiState.error,
                    fontSize = 12.sp,
                    color = Danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "아이디 찾기",
                    fontSize = 13.sp,
                    color = MutedForeground,
                    modifier = Modifier.clickable { onFindAccount() },
                )
                Text(
                    text = "  |  ",
                    fontSize = 13.sp,
                    color = MutedForeground,
                )
                Text(
                    text = "비밀번호 찾기",
                    fontSize = 13.sp,
                    color = MutedForeground,
                    modifier = Modifier.clickable { onPasswordReset() },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .gradientBorder(shape = RoundedCornerShape(50), borderWidth = 1.5.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = White),
            ) {
                Text(
                    text = "로그인",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "회원가입",
                    fontSize = 14.sp,
                    color = OnBackground,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToSignup() },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
