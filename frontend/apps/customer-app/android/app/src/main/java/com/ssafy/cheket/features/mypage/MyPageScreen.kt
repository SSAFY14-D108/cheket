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
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private val PageBackground = Color(0xFFFCFCFC)
private val CardBackground = Color(0xFFFFFFFF)
private val StrongText = Color(0xFF111111)
private val BodyText = Color(0xFF2F3B37)
private val MutedText = Color(0xFF6D8079)
private val SubtleText = Color(0xFF8FA09B)
private val BorderTint = Color(0xFFDDE9E5)
private val LargeCardShape = RoundedCornerShape(20.dp)
private val MediumCardShape = RoundedCornerShape(16.dp)

private val GradientBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xA6E2DAFF),
        Color(0x99D7F0E7),
        Color(0xA6D9DFFF),
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
    var showWithdrawWarning by remember { mutableStateOf(false) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }
    var isWithdrawing by remember { mutableStateOf(false) }

    if (showWithdrawWarning) {
        AlertDialog(
            onDismissRequest = { showWithdrawWarning = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("회원 탈퇴 전 안내", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "회원 탈퇴를 진행하면 보유 티켓, CTK 잔액, 거래내역 등 계정 정보가 함께 삭제될 수 있어요. 신중하게 진행해 주세요.",
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
                    Text("회원 탈퇴할게요")
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
                    modifier = Modifier.size(28.dp),
                )
            },
            title = { Text("정말 탈퇴하시겠어요?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "탈퇴한 계정 정보는 다시 복구할 수 없어요.",
                    lineHeight = 22.sp,
                    color = BodyText,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isWithdrawing = true
                        scope.launch {
                            runCatching { userService.deleteUser() }
                                .onSuccess { response ->
                                    Log.d("MyPageScreen", "deleteUser status=${response.httpStatusCode}")
                                    isWithdrawing = false
                                    showWithdrawConfirm = false
                                    if (response.httpStatusCode in 200..299) {
                                        Toast.makeText(context, "회원 탈퇴가 완료되었어요.", Toast.LENGTH_SHORT).show()
                                        onWithdrawSuccess()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            response.responseMessage ?: "회원 탈퇴에 실패했어요.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                                .onFailure { throwable ->
                                    Log.e("MyPageScreen", "deleteUser error", throwable)
                                    isWithdrawing = false
                                    showWithdrawConfirm = false
                                    Toast.makeText(context, "회원 탈퇴에 실패했어요.", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = !isWithdrawing,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (isWithdrawing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("탈퇴하기", color = White, fontWeight = FontWeight.SemiBold)
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
                            tint = BodyText,
                        )
                    }
                },
            )
        },
        containerColor = PageBackground,
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MutedText, strokeWidth = 2.dp)
                }
            }

            state.error != null -> {
                EmptyState(
                    title = "마이페이지 정보를 불러오지 못했어요",
                    description = state.error ?: "잠시 후 다시 시도해 주세요.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PageBackground)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ProfileCard(
                        name = state.name.ifBlank { "사용자" },
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
                        onTicketHistory = onMyTickets,
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
                        color = MutedText,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { showWithdrawWarning = true }
                            .padding(vertical = 4.dp),
                    )

                    Text(
                        text = "Cheket v1.0.0",
                        fontSize = 12.sp,
                        color = SubtleText,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
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
            .border(1.dp, BorderTint, LargeCardShape)
            .background(CardBackground, LargeCardShape)
            .clip(LargeCardShape)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(1.5.dp, BorderTint, CircleShape)
                            .background(CardBackground, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = BodyText,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "MY ACCOUNT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedText,
                            letterSpacing = 1.6.sp,
                        )
                        Text(
                            text = name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StrongText,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(1.5.dp, BorderTint, CircleShape)
                        .background(CardBackground, CircleShape)
                        .clip(CircleShape)
                        .clickable(onClick = onSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "설정",
                        tint = MutedText,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BodyText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = MutedText,
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = StrongText,
            textAlign = TextAlign.End,
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
            .border(1.dp, BorderTint, LargeCardShape)
            .background(CardBackground, LargeCardShape)
            .clip(LargeCardShape)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "WALLET",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MutedText,
                letterSpacing = 1.6.sp,
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(ctkBalance),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = StrongText,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CTK",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }

            Text(
                text = "예상 환산 금액 약 %,d원".format((ctkBalance * 1.2).toInt()),
                fontSize = 12.sp,
                color = MutedText,
            )

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
    onTicketHistory: () -> Unit,
    onCollection: () -> Unit,
    onWishlist: () -> Unit,
    onTxHistory: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderTint, LargeCardShape)
            .background(CardBackground, LargeCardShape)
            .clip(LargeCardShape)
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = "나의 활동",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StrongText,
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickLinkCard(
                    label = "티켓 내역",
                    value = "",
                    icon = Icons.Outlined.ConfirmationNumber,
                    modifier = Modifier.weight(1f),
                    onClick = onTicketHistory,
                )
                QuickLinkCard(
                    label = "컬렉션",
                    value = "",
                    icon = Icons.Outlined.Verified,
                    modifier = Modifier.weight(1f),
                    onClick = onCollection,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                    value = "",
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
            .background(CardBackground, MediumCardShape)
            .clip(MediumCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BodyText,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = StrongText,
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
            .border(2.dp, GradientBorderBrush, MediumCardShape)
            .elevatedSurface(MediumCardShape)
            .clip(MediumCardShape)
            .clickable(onClick = onClick)
            .padding(14.dp),
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
                    tint = BodyText,
                    modifier = Modifier.size(18.dp),
                )
                if (value.isNotBlank()) {
                    Text(
                        text = value,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BodyText,
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StrongText,
            )
        }
    }
}
