package com.ssafy.cheket.features.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.launch

private val V0Background = Color(0xFFFCFCFC)
private val V0Card = Color(0xFFFFFFFF)
private val V0Text = Color(0xFF111111)
private val V0TextDark = Color(0xFF333333)
private val V0Muted = Color(0xFF5C7A73)
private val V0Sub = Color(0xFF9CA3AF)
private val V0Border = Color(0xFFD8EFEA)
private val V0FooterMuted = Color(0xFF7F9891)
private val LargeCardShape = RoundedCornerShape(22.dp)
private val MediumCardShape = RoundedCornerShape(18.dp)

private val GradientBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xC2E2DAFF),
        Color(0xBDC4F7E0),
        Color(0xC2CAE6FF),
    ),
)

private val GradientBorderSoftBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x80E2DAFF),
        Color(0x75C4F7E0),
        Color(0x80CAE6FF),
    ),
)

@Composable
fun MyPageScreen(
    userService: UserService,
    onWallet: () -> Unit,
    onMyTickets: () -> Unit = {},
    onCollection: () -> Unit = {},
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
    var showWithdrawConfirm by remember { mutableStateOf(false) }
    var showWithdrawWarning by remember { mutableStateOf(false) }
    var isWithdrawing by remember { mutableStateOf(false) }

    if (showWithdrawWarning) {
        AlertDialog(
            onDismissRequest = { showWithdrawWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(32.dp),
                )
            },
            title = { Text("회원 탈퇴", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "회원 탈퇴를 진행하면 보유 티켓, CTK 잔액, 거래 내역을 다시 복구할 수 없습니다.\n\n정말 탈퇴하시겠어요?",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWithdrawWarning = false
                        showWithdrawConfirm = true
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Danger),
                ) {
                    Text("탈퇴 계속", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithdrawWarning = false }) {
                    Text("취소")
                }
            },
        )
    }

    if (showWithdrawConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isWithdrawing) showWithdrawConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(32.dp),
                )
            },
            title = { Text("한 번 더 확인", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Text(
                    "보유 티켓, CTK 잔액, 거래 내역이 모두 삭제됩니다.\n\n탈퇴 후에는 복구할 수 없습니다.",
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
                                showWithdrawConfirm = false
                                if (response.httpStatusCode in 200..299) {
                                    Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                    onWithdrawSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        response.responseMessage ?: "회원 탈퇴에 실패했습니다.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("MyPageScreen", "deleteUser() error", e)
                                isWithdrawing = false
                                showWithdrawConfirm = false
                                Toast.makeText(context, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isWithdrawing,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (isWithdrawing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("회원 탈퇴", fontWeight = FontWeight.SemiBold, color = White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showWithdrawConfirm = false },
                    enabled = !isWithdrawing,
                ) {
                    Text("취소")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "마이페이지",
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "알림",
                            tint = V0TextDark,
                        )
                    }
                },
            )
        },
        containerColor = V0Background,
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = V0Muted, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        if (state.error != null) {
            EmptyState(
                title = "마이페이지 정보를 불러오지 못했어요",
                description = state.error ?: "잠시 후 다시 시도해주세요.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(V0Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileCard(
                name = state.name.ifEmpty { "게스트" },
                phone = state.phone,
                email = state.email,
                onSettings = onSettings,
            )

            WalletCard(
                ctkBalance = state.ctkBalance,
                onWallet = onWallet,
            )

            QuickLinksSection(
                wishlistCount = state.wishlistCount,
                onMyTickets = onMyTickets,
                onCollection = onCollection,
                onWishlist = onWishlist,
                onTxHistory = onTxHistory,
            )

            GradientActionButton(
                label = "로그아웃",
                icon = Icons.AutoMirrored.Outlined.Logout,
                onClick = onLogout,
            )

            Text(
                text = "회원 탈퇴",
                fontSize = 12.sp,
                color = V0Muted,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showWithdrawWarning = true }
                    .padding(vertical = 4.dp),
            )

            Text(
                text = "Cheket v1.0.0",
                fontSize = 12.sp,
                color = V0FooterMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    phone: String,
    email: String,
    onSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, V0Border, LargeCardShape)
            .background(V0Card, LargeCardShape)
            .clip(LargeCardShape)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(2.dp, V0Border, CircleShape)
                            .background(V0Card, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = V0TextDark,
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "MY ACCOUNT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = V0Muted,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = V0Text,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, V0Border, CircleShape)
                        .background(V0Card, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = onSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "설정",
                        tint = V0Muted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "전화번호",
                    value = phone.ifBlank { "-" },
                )
                InfoRow(
                    icon = Icons.Outlined.Email,
                    label = "이메일",
                    value = email.ifBlank { "-" },
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = V0TextDark,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = V0Muted,
            )
        }

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = V0Text,
        )
    }
}

@Composable
private fun WalletCard(
    ctkBalance: Int,
    onWallet: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, V0Border, LargeCardShape)
            .background(V0Card, LargeCardShape)
            .clip(LargeCardShape)
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = "WALLET",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = V0Muted,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(ctkBalance),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = V0Text,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CTK",
                    fontSize = 14.sp,
                    color = V0Muted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "예상 환산 금액 약 %,d원".format((ctkBalance * 1.2).toInt()),
                fontSize = 13.sp,
                color = V0Muted,
            )

            Spacer(modifier = Modifier.height(16.dp))

            GradientActionButton(
                label = "지갑 보기",
                icon = Icons.Outlined.AccountBalanceWallet,
                onClick = onWallet,
                fillMaxWidth = false,
            )
        }
    }
}

@Composable
private fun QuickLinksSection(
    wishlistCount: Int,
    onMyTickets: () -> Unit,
    onCollection: () -> Unit,
    onWishlist: () -> Unit,
    onTxHistory: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, V0Border, LargeCardShape)
            .background(V0Card, LargeCardShape)
            .clip(LargeCardShape)
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "빠른 이동",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0Text,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "자주 보는 메뉴를 바로 이동할 수 있어요.",
                fontSize = 12.sp,
                color = V0Muted,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickLinkCard(
                    label = "보유티켓",
                    value = "확인",
                    icon = Icons.Outlined.ConfirmationNumber,
                    modifier = Modifier.weight(1f),
                    onClick = onMyTickets,
                )
                QuickLinkCard(
                    label = "관람완료",
                    value = "확인",
                    icon = Icons.Outlined.Badge,
                    modifier = Modifier.weight(1f),
                    onClick = onCollection,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickLinkCard(
                    label = "찜한 공연",
                    value = "${wishlistCount}건",
                    icon = Icons.Outlined.FavoriteBorder,
                    modifier = Modifier.weight(1f),
                    onClick = onWishlist,
                )
                QuickLinkCard(
                    label = "거래내역",
                    value = "확인",
                    icon = Icons.Outlined.Receipt,
                    modifier = Modifier.weight(1f),
                    onClick = onTxHistory,
                )
            }
        }
    }
}

@Composable
private fun GradientActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    fillMaxWidth: Boolean = true,
) {
    Box(
        modifier = Modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .border(2.dp, GradientBorderBrush, MediumCardShape)
            .background(V0Card, MediumCardShape)
            .clip(MediumCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = V0TextDark,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = V0Text,
            )
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
    Box(
        modifier = modifier
            .border(2.dp, GradientBorderSoftBrush, MediumCardShape)
            .elevatedSurface(MediumCardShape)
            .clip(MediumCardShape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = V0TextDark,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0TextDark,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = label,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = V0Text,
            )
        }
    }
}
