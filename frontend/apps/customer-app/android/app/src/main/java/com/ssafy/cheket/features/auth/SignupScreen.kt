package com.ssafy.cheket.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignupViewModel = viewModel(factory = SignupViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignupSuccess) {
        if (uiState.isSignupSuccess) onSignupSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Step indicator
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(2) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i < uiState.step) Primary else Muted),
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (uiState.step == 1) {
                    Step1Content(uiState, viewModel)
                } else {
                    Step2Content(uiState, viewModel)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step1Content(uiState: SignupUiState, viewModel: SignupViewModel) {
    // Name
    Column {
        Text("이름", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            placeholder = { Text("실명을 입력해주세요", fontSize = 14.sp) },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = inputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.errors["name"]?.let {
            Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
        }
    }

    // Email
    Column {
        Text("이메일", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("이메일 주소", fontSize = 14.sp) },
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = inputColors(),
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = viewModel::checkEmailDuplicate,
                enabled = uiState.email.isNotBlank() && !uiState.emailChecked,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.height(56.dp),
            ) {
                Text(
                    "중복확인",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                )
            }
        }
        uiState.errors["email"]?.let {
            Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
        }
        if (uiState.emailChecked) {
            Text("사용 가능한 이메일입니다.", fontSize = 12.sp, color = Primary, modifier = Modifier.padding(top = 4.dp))
        }
    }

    // Phone
    Column {
        Text("전화번호", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = { viewModel.onPhoneChange(it.filter { c -> c.isDigit() }.take(11)) },
                placeholder = { Text("010-0000-0000", fontSize = 14.sp) },
                visualTransformation = com.ssafy.cheket.core.ui.component.PhoneVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = inputColors(),
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = viewModel::sendSms,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.height(56.dp),
            ) {
                Text(
                    if (uiState.codeSent) "재발송" else "SMS 인증",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                )
            }
        }
        uiState.errors["phone"]?.let {
            Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
        }
    }

    // Verification code (shown after SMS sent)
    if (uiState.codeSent) {
        Column {
            Text("인증번호", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = viewModel::onCodeChange,
                    placeholder = { Text("6자리 인증번호", fontSize = 14.sp) },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = inputColors(),
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = viewModel::verifyCode,
                    enabled = !uiState.codeVerified,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(56.dp),
                ) {
                    if (uiState.codeVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Check, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Text("완료", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("확인", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            uiState.errors["code"]?.let {
                Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
            }
            if (uiState.codeVerified) {
                Text("인증이 완료되었습니다.", fontSize = 12.sp, color = Primary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Next button
    Button(
        onClick = viewModel::goToStep2,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
    ) {
        Text("다음", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
    }
}

@Composable
private fun Step2Content(uiState: SignupUiState, viewModel: SignupViewModel) {
    // Password
    Column {
        Text("비밀번호 설정", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = { Text("비밀번호 (6자 이상)", fontSize = 14.sp) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = inputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.errors["password"]?.let {
            Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
        }
    }

    // Password confirm
    Column {
        Text("비밀번호 확인", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = uiState.passwordConfirm,
            onValueChange = viewModel::onPasswordConfirmChange,
            placeholder = { Text("비밀번호 재입력", fontSize = 14.sp) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = inputColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.errors["passwordConfirm"]?.let {
            Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
        }
    }

    // Terms section
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Muted,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("약관 동의", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
            Spacer(Modifier.height(8.dp))
            Text("[필수] 서비스 이용약관 동의", fontSize = 12.sp, color = MutedForeground)
            Spacer(Modifier.height(4.dp))
            Text("[필수] 개인정보 수집 및 이용 동의", fontSize = 12.sp, color = MutedForeground)
            Spacer(Modifier.height(4.dp))
            Text("[선택] 마케팅 정보 수신 동의", fontSize = 12.sp, color = MutedForeground)
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                IconButton(
                    onClick = viewModel::toggleAgreed,
                    modifier = Modifier.size(24.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(22.dp),
                        shape = CircleShape,
                        color = if (uiState.agreedAll) Primary else Surface,
                        border = ButtonDefaults.outlinedButtonBorder(true),
                    ) {
                        if (uiState.agreedAll) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "전체 동의",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.agreedAll) Primary else OnBackground,
                )
            }
            uiState.errors["agreed"]?.let {
                Text(it, fontSize = 12.sp, color = Danger, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Signup button
    Button(
        onClick = viewModel::signup,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
    ) {
        Text("가입 완료", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = White)
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = Muted,
    unfocusedContainerColor = Muted,
)
