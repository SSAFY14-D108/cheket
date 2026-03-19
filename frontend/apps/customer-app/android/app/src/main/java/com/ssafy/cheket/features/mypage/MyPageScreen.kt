package com.ssafy.cheket.features.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

// v0 design tokens
private val V0Background = Color(0xFFFCFCFC)
private val V0Card = Color(0xFFFFFFFF)
private val V0Text = Color(0xFF111111)
private val V0TextDark = Color(0xFF333333)
private val V0Muted = Color(0xFF5C7A73)
private val V0Sub = Color(0xFF9CA3AF)
private val V0Border = Color(0xFFD8EFEA)
private val V0FooterMuted = Color(0xFF7F9891)

// v0 gradient-border brush
private val GradientBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xC2E2DAFF), // rgba(226, 218, 255, 0.76)
        Color(0xBDC4F7E0), // rgba(196, 247, 224, 0.74)
        Color(0xC2CAE6FF), // rgba(202, 230, 255, 0.76)
    ),
)

// Softer gradient for quick link cards (0.5 / 0.46 / 0.5 alpha)
private val GradientBorderSoftBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x80E2DAFF), // rgba(226, 218, 255, 0.5)
        Color(0x75C4F7E0), // rgba(196, 247, 224, 0.46)
        Color(0x80CAE6FF), // rgba(202, 230, 255, 0.5)
    ),
)

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
        containerColor = V0Background,
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = V0Muted)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Profile Section ──
            // v0: rounded-2xl border border-border bg-card p-5
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, V0Border, RoundedCornerShape(16.dp))
                    .background(V0Card, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Avatar with gradient border
                    // v0: h-14 w-14 rounded-full border-[3px] gradient border
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(3.dp, GradientBorderBrush, CircleShape)
                            .clip(CircleShape)
                            .background(V0Card),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = V0TextDark,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column {
                                // v0: text-xs font-medium uppercase tracking-[0.18em] text-muted-foreground
                                Text(
                                    "MY ACCOUNT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = V0Muted,
                                    letterSpacing = 2.sp,
                                )
                                Spacer(Modifier.height(4.dp))
                                // v0: text-lg font-bold text-foreground
                                Text(
                                    state.name.ifEmpty { "사용자" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = V0Text,
                                )
                            }

                            // Settings button with gradient border
                            // v0: gradient-border-icon-button h-9 w-9
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, GradientBorderBrush, CircleShape)
                                    .clip(CircleShape)
                                    .background(V0Card)
                                    .clickable(onClick = onSettings),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "설정",
                                    tint = V0Muted,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Phone row
                        if (state.phone.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Outlined.Phone, null, tint = V0TextDark, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("전화번호", fontSize = 13.sp, color = V0Muted)
                                Spacer(Modifier.weight(1f))
                                Text(state.phone, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = V0Text)
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Email row
                        if (state.email.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Outlined.Email, null, tint = V0TextDark, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("이메일", fontSize = 13.sp, color = V0Muted)
                                Spacer(Modifier.weight(1f))
                                Text(state.email, fontSize = 13.sp, color = V0Text)
                            }
                        }
                    }
                }
            }

            // ── Wallet Section ──
            // v0: rounded-2xl border-2 border-transparent gradient-border bg-white p-5
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(2.dp, GradientBorderBrush, RoundedCornerShape(16.dp))
                    .background(V0Card, RoundedCornerShape(16.dp))
                    .padding(20.dp),
            ) {
                Column {
                    // v0: text-xs font-medium uppercase tracking-[0.16em] text-muted-foreground
                    Text(
                        "WALLET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = V0Muted,
                        letterSpacing = 1.8.sp,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Balance row
                    // v0: text-3xl font-bold text-[#111111] + text-sm text-muted CTK
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "%,d".format(state.ctkBalance),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = V0Text,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "CTK",
                            fontSize = 14.sp,
                            color = V0Muted,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // v0: estimated KRW value
                    Text(
                        "예상 환산 금액 약 %,d원".format((state.ctkBalance * 1.2).toInt()),
                        fontSize = 13.sp,
                        color = V0Muted,
                    )

                    Spacer(Modifier.height(16.dp))

                    // v0: gradient-border-button "지갑 보기"
                    Box(
                        modifier = Modifier
                            .border(2.dp, GradientBorderBrush, RoundedCornerShape(12.dp))
                            .background(V0Card, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onWallet)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = V0TextDark,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "지갑 보기",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = V0Text,
                            )
                        }
                    }
                }
            }

            // ── Quick Links Section ──
            // v0: rounded-2xl border border-border bg-card p-4
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, V0Border, RoundedCornerShape(16.dp))
                    .background(V0Card, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Column {
                    // v0: text-sm font-semibold
                    Text("빠른 이동", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = V0Text)
                    Spacer(Modifier.height(4.dp))
                    // v0: text-xs text-muted-foreground
                    Text(
                        "자주 보는 메뉴를 바로 이동할 수 있어요.",
                        fontSize = 12.sp,
                        color = V0Muted,
                    )
                    Spacer(Modifier.height(12.dp))

                    // v0: grid grid-cols-2 gap-3
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickLinkCard("보유티켓", "0장", Icons.Outlined.ConfirmationNumber, Modifier.weight(1f), onClick = {})
                        QuickLinkCard("관람완료", "0건", Icons.Outlined.CheckCircle, Modifier.weight(1f), onClick = {})
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickLinkCard("찜한공연", "${state.wishlistCount}건", Icons.Outlined.FavoriteBorder, Modifier.weight(1f), onClick = onWishlist)
                        QuickLinkCard("거래내역", "확인", Icons.Outlined.Receipt, Modifier.weight(1f), onClick = onWalletHistory)
                    }
                }
            }

            // ── Menu Section ──
            // v0: overflow-hidden rounded-2xl border border-border bg-card
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(1.dp, V0Border, RoundedCornerShape(16.dp))
                    .background(V0Card, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Column {
                    // v0: border-b border-border px-4 py-3
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text("계정 및 서비스", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = V0Text)
                    }
                    HorizontalDivider(color = V0Border, thickness = 1.dp)

                    // v0 menu items: 공연 문의내역, 공지사항, 이벤트 및 혜택 알림, 고객센터
                    MenuItem("공연 문의내역", onClick = {})
                    HorizontalDivider(color = V0Border, thickness = 1.dp, modifier = Modifier.padding(horizontal = 0.dp))
                    MenuItem("공지사항", onClick = {})
                    HorizontalDivider(color = V0Border, thickness = 1.dp)
                    MenuItem("이벤트 및 혜택 알림", onClick = {})
                    HorizontalDivider(color = V0Border, thickness = 1.dp)
                    MenuItem("고객센터", onClick = {})
                }
            }

            // ── Logout Button ──
            // v0: gradient-border-button py-4 w-full, text-sm font-semibold text-[#111111]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, GradientBorderBrush, RoundedCornerShape(12.dp))
                    .background(V0Card, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onLogout)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        tint = V0Text,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "로그아웃",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = V0Text,
                    )
                }
            }

            // ── Withdraw link ──
            // v0: text-xs text-muted-foreground underline
            Text(
                "회원 탈퇴",
                fontSize = 12.sp,
                color = V0Muted,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showWithdrawDialog1 = true }
                    .padding(vertical = 4.dp),
            )

            // ── Version footer ──
            // v0: text-xs text-muted-foreground
            Text(
                "Cheket v1.0.0",
                fontSize = 12.sp,
                color = V0FooterMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Quick link card matching v0 design:
 * rounded-xl border-2 border-transparent gradient-border-soft
 * shadow-[0_10px_24px_rgba(15,23,42,0.04)]
 * p-4 text-left
 */
@Composable
private fun QuickLinkCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .border(2.dp, GradientBorderSoftBrush, RoundedCornerShape(12.dp))
            .elevatedSurface(RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Top row: icon left, value right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = V0TextDark, modifier = Modifier.size(16.dp))
                if (value.isNotEmpty()) {
                    Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = V0Text)
                }
            }
            Spacer(Modifier.height(20.dp))
            // Label at bottom-left
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = V0Text)
        }
    }
}

/**
 * Menu item row matching v0 design:
 * flex w-full items-center justify-between px-4 py-3.5 text-sm
 */
@Composable
private fun MenuItem(title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 14.sp, color = V0Text, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = V0Muted, modifier = Modifier.size(16.dp))
    }
}
