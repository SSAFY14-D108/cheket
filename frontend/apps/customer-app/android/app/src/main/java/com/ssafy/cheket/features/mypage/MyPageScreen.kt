package com.ssafy.cheket.features.mypage

import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun MyPageScreen(
    onWallet: () -> Unit,
    onWishlist: () -> Unit,
    onWalletHistory: () -> Unit = {},
    onTxHistory: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val user = remember { MockDataSource.mockUser }
    val tickets = remember { MockDataSource.mockTickets }
    val soldCount = remember { tickets.count { it.status == TicketStatus.AVAILABLE } }
    val usedCount = remember { tickets.count { it.status == TicketStatus.USED } }
    val wishlistCount = 3

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = { AppHeader(title = "마이페이지", onBack = onBack) },
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
                            Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                            Spacer(Modifier.height(2.dp))
                            Text("Cheket 회원", fontSize = 13.sp, color = MutedForeground)
                        }
                    }

                    HorizontalDivider(color = BorderColor)

                    // Phone
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Phone, null, tint = MutedForeground, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(user.phone, fontSize = 13.sp, color = MutedForeground)
                    }

                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, null, tint = MutedForeground, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(user.email, fontSize = 13.sp, color = MutedForeground)
                    }
                }
            }

            // CTK Balance Card - light background per v0-version2
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Primary.copy(alpha = 0.2f))
                ),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                ) {
                    Text("보유 CTK 잔액", fontSize = 12.sp, color = Primary.copy(alpha = 0.8f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "%,d CTK".format(user.ctkBalance),
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
                QuickLinkCard("보유티켓", "$soldCount", Icons.Outlined.ConfirmationNumber, Modifier.weight(1f), onClick = {})
                QuickLinkCard("관람완료", "$usedCount", Icons.Outlined.CheckCircle, Modifier.weight(1f), onClick = {})
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickLinkCard("찜한공연", "$wishlistCount", Icons.Outlined.FavoriteBorder, Modifier.weight(1f), onClick = onWishlist)
                QuickLinkCard("거래내역", "", Icons.Outlined.Receipt, Modifier.weight(1f), onClick = onTxHistory)
            }

            // Settings
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSettings),
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
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
                    .clickable { /* TODO */ },
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
            Modifier.fillMaxWidth().padding(16.dp),
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
