package com.ssafy.cheket.features.wallet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

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
        topBar = { AppHeader(title = "지갑", onBack = onBack) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Balance Card - light gradient per v0-version2
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Primary.copy(alpha = 0.2f))
                ),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Primary.copy(alpha = 0.2f), Primary.copy(alpha = 0.05f))
                            )
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Outlined.AccountBalanceWallet, null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text("CTK 잔액", fontSize = 14.sp, color = Primary.copy(alpha = 0.8f))
                    }

                    Text(
                        "%,d CTK".format(balance),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                    )

                    // Wallet address in separated section
                    HorizontalDivider(color = Primary.copy(alpha = 0.15f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Background.copy(alpha = 0.4f),
                        ) {
                            Text(
                                user.walletAddress,
                                fontSize = 12.sp,
                                color = MutedForeground,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(user.walletAddress))
                                Toast.makeText(context, "복사되었습니다!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(Icons.Outlined.ContentCopy, "복사", tint = Primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Test CTK Top-up
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("테스트 CTK 충전", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                    // 2x2 grid per v0-version2
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(5000, 10000).forEach { amount ->
                                TopUpButton(amount, Modifier.weight(1f)) {
                                    balance += amount
                                    Toast.makeText(context, "%,d CTK 충전 완료".format(amount), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(30000, 50000).forEach { amount ->
                                TopUpButton(amount, Modifier.weight(1f)) {
                                    balance += amount
                                    Toast.makeText(context, "%,d CTK 충전 완료".format(amount), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    // Disclaimer
                    Text(
                        "실제 충전 기능은 앱 출시 후 지원됩니다.",
                        fontSize = 11.sp,
                        color = SubText,
                    )
                }
            }

            // Wallet history button
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onWalletHistory),
                shape = RoundedCornerShape(12.dp),
                color = Muted,
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "지갑 거래 내역",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OnBackground,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(Icons.Outlined.ChevronRight, null, tint = SubText, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TopUpButton(amount: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            "%,d CTK".format(amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
