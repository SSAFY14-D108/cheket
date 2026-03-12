package com.ssafy.cheket.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssafy.cheket.Routes
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.Surface

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "홈", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Routes.SHOWS, "탐색", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
    BottomNavItem(Routes.RESALE, "2차거래소", Icons.Filled.LocalOffer, Icons.Outlined.LocalOffer),
    BottomNavItem(Routes.MY_TICKETS, "내 티켓", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
    BottomNavItem(Routes.COLLECTION, "컬렉션", Icons.Filled.CollectionsBookmark, Icons.Outlined.CollectionsBookmark),
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
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = MutedForeground,
                    unselectedTextColor = MutedForeground,
                    indicatorColor = Surface,
                ),
            )
        }
    }
}
