package com.ssafy.cheket.features.wallet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Surface

private val WalletCardShape = RoundedCornerShape(24.dp)
private val WalletButtonShape = RoundedCornerShape(18.dp)
private val WalletBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xD8E1DBFF),
        Color(0xD7EAE3FF),
        Color(0xD8E4F1FF),
    ),
)
private val WalletSoftBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xBFE1DBFF),
        Color(0xBFEAE3FF),
        Color(0xBFE4F1FF),
    ),
)
private val WalletSurface = Color(0xFFFFFFFF)
private val WalletAddressBg = Color(0xFFF2F4F7)
private val WalletChargeButtonBg = Color(0xFFF3F4F7)
private val WalletMuted = Color(0xFF607971)
private val WalletFooter = Color(0xFF7B8F88)
private val WalletIconTint = Color(0xFF2F3433)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onWalletHistory: () -> Unit = {},
    onBack: () -> Unit,
) {
    val user = remember { MockDataSource.mockUser }
    var balance by remember { mutableIntStateOf(user.ctkBalance) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "지갑",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnBackground,
                        )
                        TutorialHelpButton(
                            tutorialId = TutorialId.WALLET,
                            tint = WalletIconTint,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = OnBackground,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "알림",
                            tint = WalletIconTint,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, WalletBorderBrush, WalletCardShape)
                    .background(WalletSurface, WalletCardShape)
                    .clip(WalletCardShape)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = WalletIconTint,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "CTK 잔액",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%,d".format(balance),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CTK",
                            fontSize = 18.sp,
                            color = WalletMuted,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }

                    Text(
                        text = "약 %,d원".format((balance * 1.2).toInt()),
                        fontSize = 16.sp,
                        color = WalletMuted,
                    )

                    HorizontalDivider(color = Color(0xFFE5ECE8))

                    Text(
                        text = "지갑 주소",
                        fontSize = 14.sp,
                        color = WalletMuted,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(WalletAddressBg)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = user.walletAddress,
                            fontSize = 14.sp,
                            color = OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(user.walletAddress))
                                Toast.makeText(context, "지갑 주소를 복사했어요.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "복사",
                                tint = WalletMuted,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WalletSurface, WalletCardShape)
                    .clip(WalletCardShape)
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "테스트용 CTK 충전",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            listOf(5000, 10000).forEach { amount ->
                                ChargeButton(amount = amount, modifier = Modifier.weight(1f)) {
                                    balance += amount
                                    Toast.makeText(context, "%,d CTK 충전 완료".format(amount), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            listOf(30000, 50000).forEach { amount ->
                                ChargeButton(amount = amount, modifier = Modifier.weight(1f)) {
                                    balance += amount
                                    Toast.makeText(context, "%,d CTK 충전 완료".format(amount), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    Text(
                        text = "실제 충전 기능은 추후 연결 예정입니다.",
                        fontSize = 13.sp,
                        color = WalletFooter,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, WalletSoftBorderBrush, WalletButtonShape)
                    .background(WalletSurface, WalletButtonShape)
                    .clip(WalletButtonShape)
                    .clickable(onClick = onWalletHistory)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "지갑 거래 내역",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = WalletMuted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ChargeButton(
    amount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = WalletButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = WalletChargeButtonBg,
            contentColor = OnBackground,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "%,d CTK".format(amount),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
