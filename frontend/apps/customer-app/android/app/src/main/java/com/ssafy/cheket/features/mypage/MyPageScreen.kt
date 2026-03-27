package com.ssafy.cheket.features.mypage

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.MusicNote
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.CardBg
import com.ssafy.cheket.ui.theme.Danger
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryLight
import com.ssafy.cheket.ui.theme.White
import kotlinx.coroutines.launch

private val PageBackground = Background
private val PanelShape = RoundedCornerShape(24.dp)
private val MenuShape = RoundedCornerShape(20.dp)
private val HeroBorder = Color(0xFFDCE5F1)
private val HeroSurface = Color(0xFFF7F9FC)
private val SecondarySurface = Color(0xFFF6F7FA)
private val SecondaryText = Color(0xFF6B7280)
private val TertiaryText = Color(0xFF98A2B3)

@Composable
fun MyPageScreen(
    userService: UserService,
    onWallet: () -> Unit,
    onMyTickets: () -> Unit = {},
    onCollection: () -> Unit = {},
    onWishlist: () -> Unit,
    onWalletHistory: () -> Unit = {},
    onTxHistory: () -> Unit = {},
    onMyShows: () -> Unit = {},
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
        com.ssafy.cheket.core.ui.component.CheketDialog(
            title = "회원 탈퇴 안내",
            message = "탈퇴하면 보유 티켓, 지갑 정보, 찜한 공연 정보가 함께 삭제될 수 있습니다. 계속 진행하시겠어요?",
            confirmText = "탈퇴 진행",
            dismissText = "취소",
            onConfirm = { showWithdrawWarning = false; showWithdrawConfirm = true },
            onDismiss = { showWithdrawWarning = false },
            isDanger = true,
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
                    text = "이 작업은 되돌릴 수 없습니다.",
                    lineHeight = 22.sp,
                    color = SecondaryText,
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
                                        Toast.makeText(context, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                        onWithdrawSuccess()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            response.responseMessage ?: "회원 탈퇴에 실패했습니다.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }
                                .onFailure { throwable ->
                                    Log.e("MyPageScreen", "deleteUser error", throwable)
                                    isWithdrawing = false
                                    showWithdrawConfirm = false
                                    Toast.makeText(context, "회원 탈퇴에 실패했습니다.", Toast.LENGTH_SHORT).show()
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
        topBar = { AppHeader(title = "마이페이지") },
        containerColor = PageBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }
            }

            state.error != null -> {
                EmptyState(
                    title = "마이페이지를 불러오지 못했어요",
                    description = "잠시 후 다시 시도해주세요.",
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    ProfileHero(
                        name = state.name.ifBlank { "CHEKET USER" },
                        phone = state.phone,
                        email = state.email,
                        onSettings = onSettings,
                    )

                    BalancePanel(
                        ctkBalance = state.ctkBalance,
                        walletAddress = state.walletAddress,
                        onWallet = onWallet,
                    )

                    MenuList(
                        wishlistCount = state.wishlistCount,
                        onTicketHistory = onMyTickets,
                        onCollection = onCollection,
                        onWishlist = onWishlist,
                        onTxHistory = onTxHistory,
                        onMyShows = onMyShows,
                        onSettings = onSettings,
                    )

                    SecondaryActions(
                        onLogout = onLogout,
                        onWithdraw = { showWithdrawWarning = true },
                    )

                    Text(
                        text = "Cheket v1.0.0",
                        fontSize = 12.sp,
                        color = TertiaryText,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHero(
    name: String,
    phone: String,
    email: String,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HeroBorder, PanelShape)
            .background(HeroSurface, PanelShape)
            .clip(PanelShape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )
                }
            }

            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "설정",
                    tint = SecondaryText,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileMetaRow(
                icon = Icons.Outlined.Phone,
                label = "전화번호",
                value = phone.ifBlank { "-" },
            )
            ProfileMetaRow(
                icon = Icons.Outlined.Email,
                label = "이메일",
                value = email.ifBlank { "-" },
            )
        }
    }
}

@Composable
private fun ProfileMetaRow(
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
                tint = SecondaryText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = SecondaryText,
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnBackground,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun BalancePanel(
    ctkBalance: Int,
    walletAddress: String,
    onWallet: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, PanelShape)
            .border(1.dp, BorderColor, PanelShape)
            .clip(PanelShape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "보유 자산",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TertiaryText,
            letterSpacing = 1.2.sp,
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "%,d".format(ctkBalance),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SSF",
                fontSize = 13.sp,
                color = SecondaryText,
                modifier = Modifier.padding(bottom = 5.dp),
            )
        }

        if (walletAddress.isNotBlank()) {
            Text(
                text = shortWalletAddress(walletAddress),
                fontSize = 12.sp,
                color = SecondaryText,
            )
        } else {
            Text(
                text = "지갑 주소를 아직 불러오지 못했어요",
                fontSize = 12.sp,
                color = SecondaryText,
            )
        }

        TextButton(
            onClick = onWallet,
            colors = ButtonDefaults.textButtonColors(contentColor = Primary),
            modifier = Modifier.align(Alignment.Start),
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("지갑 보기", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MenuList(
    wishlistCount: Int,
    onTicketHistory: () -> Unit,
    onCollection: () -> Unit,
    onWishlist: () -> Unit,
    onTxHistory: () -> Unit,
    onMyShows: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, MenuShape)
            .border(1.dp, BorderColor, MenuShape)
            .clip(MenuShape)
            .padding(vertical = 6.dp),
    ) {
        MenuRow(
            icon = Icons.Outlined.ConfirmationNumber,
            title = "내 티켓",
            subtitle = "예매 내역과 입장 티켓을 확인하세요",
            onClick = onTicketHistory,
        )
        MenuDivider()
        MenuRow(
            icon = Icons.Outlined.CollectionsBookmark,
            title = "컬렉션",
            subtitle = "보유한 NFT 티켓 컬렉션을 둘러보세요",
            onClick = onCollection,
        )
        MenuDivider()
        MenuRow(
            icon = Icons.Outlined.FavoriteBorder,
            title = "찜한 공연",
            subtitle = "관심 공연 ${wishlistCount}개",
            trailing = "$wishlistCount",
            onClick = onWishlist,
        )
        MenuDivider()
        MenuRow(
            icon = Icons.Outlined.Receipt,
            title = "거래 내역",
            subtitle = "거래와 정산 내역을 확인하세요",
            onClick = onTxHistory,
        )
        MenuDivider()
        MenuRow(
            icon = Icons.Outlined.MusicNote,
            title = "참여 공연",
            subtitle = "아티스트로 참여한 공연과 정산을 확인하세요",
            onClick = onMyShows,
        )
        MenuDivider()
        MenuRow(
            icon = Icons.Outlined.Settings,
            title = "설정",
            subtitle = "비밀번호와 앱 설정을 관리하세요",
            onClick = onSettings,
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(SecondarySurface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnBackground,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground,
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SecondaryText,
            )
        }

        if (!trailing.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrimaryLight)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = trailing,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = TertiaryText,
            )
        }
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = BorderColor,
        thickness = 1.dp,
    )
}

@Composable
private fun SecondaryActions(
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = onLogout,
            colors = ButtonDefaults.textButtonColors(contentColor = OnBackground),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("로그아웃", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun shortWalletAddress(address: String): String {
    if (address.length <= 14) return address
    return "${address.take(8)}...${address.takeLast(6)}"
}
