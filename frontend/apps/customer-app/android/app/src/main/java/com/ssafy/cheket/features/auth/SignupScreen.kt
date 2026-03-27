package com.ssafy.cheket.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.PhoneVisualTransformation
import com.ssafy.cheket.core.ui.component.gradientBorder
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.White

private val AuthInputBackground = Color(0xFFF6F7F8)
private val AuthInputBorder = Color(0xFFE1E5E8)
private val AuthFieldShape = RoundedCornerShape(24.dp)

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignupViewModel = viewModel(factory = SignupViewModel.Factory),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(uiState.isSignupSuccess) {
        if (uiState.isSignupSuccess) onSignupSuccess()
    }

    if (uiState.isLoading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MutedForeground,
                    strokeWidth = 3.dp,
                )
            },
            text = {
                Text(
                    text = "회원가입 처리 중...",
                    fontSize = 15.sp,
                    color = OnBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            },
            containerColor = White,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Scaffold(
        topBar = { AppHeader(title = "회원가입", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index < uiState.step) Color(0xFFD6DCE2) else Muted,
                                shape = RoundedCornerShape(50),
                            ),
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (uiState.step == 1) {
                    SignupStepOne(uiState = uiState, viewModel = viewModel)
                } else {
                    SignupStepTwo(uiState = uiState, viewModel = viewModel)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SignupStepOne(
    uiState: SignupUiState,
    viewModel: SignupViewModel,
) {
    SignupInputBlock("이름", uiState.errors["name"]) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            placeholder = { Text("이름을 입력해 주세요", fontSize = 14.sp) },
            singleLine = true,
            shape = AuthFieldShape,
            colors = signupInputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    SignupInputBlock("이메일", uiState.errors["email"]) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("이메일을 입력해 주세요", fontSize = 14.sp) },
                singleLine = true,
                shape = AuthFieldShape,
                colors = signupInputColors(),
                modifier = Modifier.weight(1f),
            )
            SecondaryAuthButton(
                text = "중복 확인",
                enabled = uiState.email.isNotBlank() && !uiState.emailChecked,
                onClick = viewModel::checkEmailDuplicate,
                modifier = Modifier.height(52.dp),
            )
        }
        if (uiState.emailChecked) {
            Text(
                text = "사용 가능한 이메일이에요.",
                fontSize = 12.sp,
                color = MutedForeground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }

    SignupInputBlock("전화번호", uiState.errors["phone"]) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = {
                    viewModel.onPhoneChange(it.filter(Char::isDigit).take(11))
                },
                placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                visualTransformation = PhoneVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = AuthFieldShape,
                colors = signupInputColors(),
                modifier = Modifier.weight(1f),
            )
            SecondaryAuthButton(
                text = if (uiState.codeSent) "재전송" else "SMS 전송",
                onClick = viewModel::sendSms,
                modifier = Modifier.height(52.dp),
            )
        }
        if (uiState.codeSent) {
            Text(
                text = "인증번호를 발송했어요.",
                fontSize = 12.sp,
                color = MutedForeground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }

    if (uiState.codeSent) {
        SignupInputBlock("인증번호", uiState.errors["code"]) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = viewModel::onCodeChange,
                    placeholder = { Text("6자리 인증번호", fontSize = 14.sp) },
                    singleLine = true,
                    shape = AuthFieldShape,
                    colors = signupInputColors(),
                    modifier = Modifier.weight(1f),
                )
                SecondaryAuthButton(
                    text = if (uiState.codeVerified) "인증됨" else "확인",
                    enabled = !uiState.codeVerified,
                    onClick = viewModel::verifyCode,
                    modifier = Modifier.height(52.dp),
                )
            }
            if (uiState.codeVerified) {
                Text(
                    text = "인증이 완료되었어요.",
                    fontSize = 12.sp,
                    color = MutedForeground,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    PrimaryAuthButton(text = "다음", onClick = viewModel::goToStep2)
}

@Composable
private fun SignupStepTwo(
    uiState: SignupUiState,
    viewModel: SignupViewModel,
) {
    SignupInputBlock("비밀번호 설정", uiState.errors["password"]) {
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = { Text("비밀번호 (6자 이상)", fontSize = 14.sp) },
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.showPassword) "숨기기" else "보기",
                        tint = MutedForeground,
                    )
                }
            },
            visualTransformation = if (uiState.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = AuthFieldShape,
            colors = signupInputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    SignupInputBlock("비밀번호 확인", uiState.errors["passwordConfirm"]) {
        OutlinedTextField(
            value = uiState.passwordConfirm,
            onValueChange = viewModel::onPasswordConfirmChange,
            placeholder = { Text("비밀번호를 다시 입력해 주세요", fontSize = 14.sp) },
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordConfirmVisibility) {
                    Icon(
                        imageVector = if (uiState.showPasswordConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.showPasswordConfirm) "숨기기" else "보기",
                        tint = MutedForeground,
                    )
                }
            },
            visualTransformation = if (uiState.showPasswordConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = AuthFieldShape,
            colors = signupInputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    PrimaryAuthButton(text = "회원가입 완료", onClick = viewModel::signup)
}

@Composable
private fun SignupInputBlock(
    label: String,
    error: String?,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        content()
        if (!error.isNullOrBlank()) {
            Text(text = error, fontSize = 12.sp, color = Danger)
        }
    }
}

@Composable
private fun PrimaryAuthButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .gradientBorder(shape = AuthFieldShape, borderWidth = 1.5.dp),
        shape = AuthFieldShape,
        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = OnBackground),
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
    }
}

@Composable
private fun SecondaryAuthButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.gradientBorder(shape = AuthFieldShape, borderWidth = 1.5.dp),
        shape = AuthFieldShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = OnBackground,
            disabledContainerColor = Color(0xFFE5E7EB),
            disabledContentColor = MutedForeground,
        ),
        border = BorderStroke(0.dp, Color.Transparent),
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun signupInputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AuthInputBorder,
    unfocusedBorderColor = AuthInputBorder,
    focusedContainerColor = AuthInputBackground,
    unfocusedContainerColor = AuthInputBackground,
)
