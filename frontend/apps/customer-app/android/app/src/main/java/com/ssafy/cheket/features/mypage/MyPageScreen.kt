package com.ssafy.cheket.features.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MyPageScreen(
    userService: UserService,
    onWallet: () -> Unit,
    onWishlist: () -> Unit,
    onWalletHistory: () -> Unit = {},
    onTxHistory: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit,
    onWithdrawSuccess: () -> Unit = onLogout,
    onBack: () -> Unit,
    viewModel: MyPageViewModel = viewModel(factory = MyPageViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 회원탈퇴 2단계 다이얼로그 상태
    var showWithdrawDialog1 by remember { mutableStateOf(false) }
    var showWithdrawDialog2 by remember { mutableStateOf(false) }
    var isWithdrawing by remember { mutableStateOf(false) }

    // 1단계: 정말 탈퇴하시겠습니까?
    if (showWithdrawDialog1) {
        AlertDialog(
            onDismissRequest = { showWithdrawDialog1 = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = Danger, modifier = Modifier.size(32.dp)) },
            title = { Text("회원 탈퇴", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "정말 탈퇴하시겠습니까?\n\n탈퇴 시 모든 데이터가 삭제되며\n복구할 수 없습니다.",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawDialog1 = false
                        showWithdrawDialog2 = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Danger),
                ) { Text("탈퇴 진행", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawDialog1 = false }) {
                    Text("취소")
                }
            },
        )
    }

    // 2단계: 최종 확인
    if (showWithdrawDialog2) {
        AlertDialog(
            onDismissRequest = { if (!isWithdrawing) showWithdrawDialog2 = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = Danger, modifier = Modifier.size(32.dp)) },
            title = { Text("최종 확인", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "보유한 티켓, CTK 잔액, 거래 내역 등\n모든 정보가 영구 삭제됩니다.\n\n정말로 탈퇴하시겠습니까?",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = Danger,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isWithdrawing = true
                        scope.launch {
                            try {
                                val response = userService.deleteUser()
                                Log.d("MyPageScreen", "deleteUser() statusCode=${response.httpStatusCode}")
                                isWithdrawing = false
                                showWithdrawDialog2 = false
                                if (response.httpStatusCode in 200..299) {
                                    Toast.makeText(context, "회원 탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                                    onWithdrawSuccess()
                                } else {
                                    Toast.makeText(context, response.responseMessage ?: "회원 탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e("MyPageScreen", "deleteUser() error", e)
                                isWithdrawing = false
                                showWithdrawDialog2 = false
                                Toast.makeText(context, "회원 탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isWithdrawing,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (isWithdrawing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = White, strokeWidth = 2.dp)
                    } else {
                        Text("회원 탈퇴", fontWeight = FontWeight.SemiBold, color = White)
                    }
                }
            },
            dismissButton = {
                if (!isWithdrawing) {
                    TextButton(onClick = { showWithdrawDialog2 = false }) {
                        Text("취소")
                    }
                }
            },
        )
    }

    Scaffold(
        topBar = { AppHeader(title = "마이페이지", onBack = onBack) },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Avatar + Name + Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                state.name.ifEmpty { "사용자" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text("Cheket 회원", fontSize = 13.sp, color = MutedForeground)
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Phone
                    if (state.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Phone, null, tint = MutedForeground, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(state.phone, fontSize = 13.sp, color = MutedForeground)
                        }
                    }

                    // Email
                    if (state.email.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Email, null, tint = MutedForeground, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(state.email, fontSize = 13.sp, color = MutedForeground)
                        }
                    }
                }
            }

            // CTK Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Primary.copy(alpha = 0.2f))
                ),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                ) {
                    Text("보유 CTK 잔액", fontSize = 12.sp, color = Primary.copy(alpha = 0.8f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "%,d CTK".format(state.ctkBalance),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onWallet,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("지갑 보기", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = White)
                    }
                }
            }

            // Quick Links Grid (2x2)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickLinkCard("보유티켓", "", Icons.Outlined.ConfirmationNumber, Modifier.weight(1f), onClick = {})
                QuickLinkCard("관람완료", "", Icons.Outlined.CheckCircle, Modifier.weight(1f), onClick = {})
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickLinkCard("찜한공연", "${state.wishlistCount}", Icons.Outlined.FavoriteBorder, Modifier.weight(1f), onClick = onWishlist)
                QuickLinkCard("거래내역", "", Icons.Outlined.Receipt, Modifier.weight(1f), onClick = onWalletHistory)
            }

            // Settings
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSettings),
                shape = RoundedCornerShape(12.dp),
                color = Muted,
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Settings, null, tint = MutedForeground, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("설정", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnBackground, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, null, tint = SubText, modifier = Modifier.size(20.dp))
                }
            }

            // Menu section
            Text("계정 및 서비스", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedForeground, modifier = Modifier.padding(top = 4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column {
                    MenuItem(Icons.Outlined.Description, "이용약관", onClick = {})
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(Icons.Outlined.Shield, "개인정보처리방침", onClick = {})
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(Icons.AutoMirrored.Outlined.HelpOutline, "공연 문의내역", onClick = {})
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(horizontal = 16.dp))
                    MenuItem(Icons.Outlined.Notifications, "공지사항", onClick = {})
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Danger.copy(alpha = 0.3f))
                ),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("로그아웃", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            // Withdraw link
            Text(
                "회원 탈퇴",
                fontSize = 12.sp,
                color = MutedForeground,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showWithdrawDialog1 = true },
            )

            // Version
            Text(
                "Cheket v1.0.0",
                fontSize = 11.sp,
                color = SubText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickLinkCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = OnBackground)
            if (value.isNotEmpty()) {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnBackground)
            }
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, color = OnBackground, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SubText, modifier = Modifier.size(20.dp))
    }
}
