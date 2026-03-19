package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.Routes
import com.ssafy.cheket.ui.theme.Surface

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

// v0 color tokens for bottom nav
private val NavActiveColor = Color(0xFF374151)
private val NavActiveIconColor = Color(0xFF374151)
private val NavInactiveIconColor = Color(0xFFC4CAD4)
private val NavInactiveTextColor = Color(0xFF9CA3AF)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "메인", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.SHOWS, "공연", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
    BottomNavItem(Routes.RESALE, "2차거래소", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer),
    BottomNavItem(Routes.MY_TICKETS, "내 티켓", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
    BottomNavItem(Routes.MY_PAGE, "마이", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
)

@Composable
fun CheketBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(containerColor = Surface) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavActiveIconColor,
                    selectedTextColor = NavActiveColor,
                    unselectedIconColor = NavInactiveIconColor,
                    unselectedTextColor = NavInactiveTextColor,
                    indicatorColor = Surface,
                ),
            )
        }
    }
}
