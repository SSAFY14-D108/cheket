package com.ssafy.cheket

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.CheketBottomBar
import com.ssafy.cheket.features.auth.LoginScreen
import com.ssafy.cheket.features.auth.SignupScreen
import com.ssafy.cheket.features.collection.ArchiveScreen
import com.ssafy.cheket.features.collection.CollectibleTicketDetailScreen
import com.ssafy.cheket.features.collection.CollectionScreen
import com.ssafy.cheket.features.concerts.ConcertsScreen
import com.ssafy.cheket.features.event.EventDetailScreen
import com.ssafy.cheket.features.home.HomeScreen
import com.ssafy.cheket.features.mypage.MyPageScreen
import com.ssafy.cheket.features.mytickets.MyTicketsScreen
import com.ssafy.cheket.features.mytickets.QrCheckinScreen
import com.ssafy.cheket.features.mytickets.TicketDetailScreen
import com.ssafy.cheket.features.purchase.PaymentScreen
import com.ssafy.cheket.features.purchase.PurchaseFailedScreen
import com.ssafy.cheket.features.purchase.SeatSelectionScreen
import com.ssafy.cheket.features.purchase.WaitingQueueScreen
import com.ssafy.cheket.features.resale.ResaleCreateScreen
import com.ssafy.cheket.features.resale.ResaleDetailScreen
import com.ssafy.cheket.features.resale.ResalePurchaseCompleteScreen
import com.ssafy.cheket.features.resale.ResaleScreen
import com.ssafy.cheket.features.transfer.TransferCompleteScreen
import com.ssafy.cheket.features.transfer.TransferFailedScreen
import com.ssafy.cheket.features.transfer.TransferScreen
import com.ssafy.cheket.features.wallet.WalletScreen
import com.ssafy.cheket.features.wallet.WalletHistoryScreen
import com.ssafy.cheket.features.wallet.TxHistoryScreen
import com.ssafy.cheket.features.wallet.WithdrawScreen
import com.ssafy.cheket.features.wishlist.WishlistScreen
import com.ssafy.cheket.features.settings.SettingsScreen
import com.ssafy.cheket.features.settings.PasswordChangeScreen
import com.ssafy.cheket.features.auth.FindAccountScreen
import com.ssafy.cheket.features.auth.PasswordResetScreen
import com.ssafy.cheket.features.resale.ResaleListScreen
import com.ssafy.cheket.features.resale.ResaleTicketsScreen

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val CONCERTS = "concerts"
    const val RESALE = "resale"
    const val MY_TICKETS = "my_tickets"
    const val COLLECTION = "collection"

    // Detail / Flow screens
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val WAITING_QUEUE = "waiting_queue/{eventId}"
    const val SEAT_SELECTION = "seat_selection/{eventId}"
    const val PAYMENT = "payment/{eventId}"
    const val PURCHASE_FAILED = "purchase_failed/{eventId}/{reason}"
    const val TICKET_DETAIL = "ticket_detail/{ticketId}"
    const val QR_CHECKIN = "qr_checkin/{ticketId}"
    const val TRANSFER = "transfer/{ticketId}"
    const val TRANSFER_COMPLETE = "transfer_complete/{ticketId}"
    const val TRANSFER_FAILED = "transfer_failed/{ticketId}"
    const val RESALE_DETAIL = "resale_detail/{resaleItemId}"
    const val RESALE_PURCHASE_COMPLETE = "resale_purchase_complete/{ticketId}"
    const val RESALE_CREATE = "resale_create/{ticketId}"
    const val WISHLIST = "wishlist"
    const val MY_PAGE = "my_page"
    const val WALLET = "wallet"
    const val ARCHIVE = "archive"
    const val COLLECTIBLE_DETAIL = "collectible_detail/{ticketId}"

    // New v0 screens
    const val WALLET_HISTORY = "wallet_history"
    const val TX_HISTORY = "tx_history"
    const val WITHDRAW = "withdraw"
    const val SETTINGS = "settings"
    const val PASSWORD_CHANGE = "password_change"
    const val FIND_ACCOUNT = "find_account"
    const val PASSWORD_RESET = "password_reset"
    const val RESALE_LIST = "resale_list"
    const val RESALE_TICKETS = "resale_tickets/{eventId}"

    // Helper functions for building routes with args
    fun eventDetail(eventId: String) = "event_detail/$eventId"
    fun waitingQueue(eventId: String) = "waiting_queue/$eventId"
    fun seatSelection(eventId: String) = "seat_selection/$eventId"
    fun payment(eventId: String) = "payment/$eventId"
    fun purchaseFailed(eventId: String, reason: String) = "purchase_failed/$eventId/${java.net.URLEncoder.encode(reason, "UTF-8")}"
    fun ticketDetail(ticketId: String) = "ticket_detail/$ticketId"
    fun qrCheckin(ticketId: String) = "qr_checkin/$ticketId"
    fun transfer(ticketId: String) = "transfer/$ticketId"
    fun transferComplete(ticketId: String) = "transfer_complete/$ticketId"
    fun transferFailed(ticketId: String) = "transfer_failed/$ticketId"
    fun resaleDetail(resaleItemId: String) = "resale_detail/$resaleItemId"
    fun resalePurchaseComplete(ticketId: String) = "resale_purchase_complete/$ticketId"
    fun resaleCreate(ticketId: String) = "resale_create/$ticketId"
    fun collectibleDetail(ticketId: String) = "collectible_detail/$ticketId"
    fun resaleTickets(eventId: String) = "resale_tickets/$eventId"
}

val bottomTabRoutes = listOf(
    Routes.HOME, Routes.CONCERTS, Routes.RESALE, Routes.MY_TICKETS, Routes.COLLECTION,
)

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                CheketBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ── Auth ──
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToSignup = { navController.navigate(Routes.SIGNUP) },
                    onFindAccount = { navController.navigate(Routes.FIND_ACCOUNT) },
                    onPasswordReset = { navController.navigate(Routes.PASSWORD_RESET) },
                )
            }
            composable(Routes.SIGNUP) {
                SignupScreen(
                    onSignupSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Main tabs ──
            composable(Routes.HOME) {
                HomeScreen(
                    appContainer = appContainer,
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                    onMyPage = { navController.navigate(Routes.MY_PAGE) },
                )
            }
            composable(Routes.CONCERTS) {
                ConcertsScreen(
                    appContainer = appContainer,
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                )
            }
            composable(Routes.RESALE) {
                ResaleScreen(
                    appContainer = appContainer,
                    onResaleItemClick = { eventId -> navController.navigate(Routes.resaleTickets(eventId)) },
                )
            }
            composable(Routes.MY_TICKETS) {
                MyTicketsScreen(
                    appContainer = appContainer,
                    onTicketClick = { ticketId -> navController.navigate(Routes.ticketDetail(ticketId)) },
                )
            }
            composable(Routes.COLLECTION) {
                CollectionScreen(appContainer = appContainer)
            }

            // ── Event Detail ──
            composable(
                route = Routes.EVENT_DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateToQueue = { navController.navigate(Routes.waitingQueue(it)) },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Purchase Flow ──
            composable(
                route = Routes.WAITING_QUEUE,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                WaitingQueueScreen(
                    eventId = eventId,
                    onComplete = { navController.navigate(Routes.seatSelection(it)) {
                        popUpTo(Routes.waitingQueue(eventId)) { inclusive = true }
                    } },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.SEAT_SELECTION,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                SeatSelectionScreen(
                    eventId = eventId,
                    onNavigateToPayment = { navController.navigate(Routes.payment(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.PAYMENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                PaymentScreen(
                    eventId = eventId,
                    onSuccess = {
                        navController.navigate(Routes.MY_TICKETS) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onFailure = { evtId, reason ->
                        navController.navigate(Routes.purchaseFailed(evtId, reason)) {
                            popUpTo(Routes.eventDetail(evtId)) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.PURCHASE_FAILED,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType },
                    navArgument("reason") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val reason = try {
                    java.net.URLDecoder.decode(backStackEntry.arguments?.getString("reason") ?: "", "UTF-8")
                } catch (_: Exception) { backStackEntry.arguments?.getString("reason") ?: "" }
                PurchaseFailedScreen(
                    eventId = eventId,
                    reason = reason,
                    onRetry = { navController.navigate(Routes.eventDetail(it)) {
                        popUpTo(Routes.HOME)
                    } },
                    onGoHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    } },
                )
            }

            // ── Ticket Detail & Actions ──
            composable(
                route = Routes.TICKET_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TicketDetailScreen(
                    ticketId = ticketId,
                    onQrCheckin = { navController.navigate(Routes.qrCheckin(it)) },
                    onTransfer = { navController.navigate(Routes.transfer(it)) },
                    onResaleCreate = { navController.navigate(Routes.resaleCreate(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.QR_CHECKIN,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                QrCheckinScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Transfer Flow ──
            composable(
                route = Routes.TRANSFER,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TransferScreen(
                    ticketId = ticketId,
                    onTransferComplete = { navController.navigate(Routes.transferComplete(it)) {
                        popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                    } },
                    onTransferFailed = { navController.navigate(Routes.transferFailed(it)) {
                        popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                    } },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.TRANSFER_COMPLETE,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TransferCompleteScreen(
                    ticketId = ticketId,
                    onGoToTickets = { navController.navigate(Routes.MY_TICKETS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                    } },
                    onGoHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    } },
                )
            }
            composable(
                route = Routes.TRANSFER_FAILED,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TransferFailedScreen(
                    ticketId = ticketId,
                    onRetry = { navController.navigate(Routes.transfer(it)) {
                        popUpTo(Routes.MY_TICKETS)
                    } },
                    onGoToTickets = { navController.navigate(Routes.MY_TICKETS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                    } },
                )
            }

            // ── Resale Flow ──
            composable(
                route = Routes.RESALE_DETAIL,
                arguments = listOf(navArgument("resaleItemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val resaleItemId = backStackEntry.arguments?.getString("resaleItemId") ?: ""
                ResaleDetailScreen(
                    resaleItemId = resaleItemId,
                    onPurchaseComplete = { ticketId ->
                        navController.navigate(Routes.resalePurchaseComplete(ticketId)) {
                            popUpTo(Routes.RESALE)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.RESALE_PURCHASE_COMPLETE,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                ResalePurchaseCompleteScreen(
                    purchasedTicketId = ticketId,
                    onGoToTicketDetail = { navController.navigate(Routes.ticketDetail(it)) {
                        popUpTo(Routes.HOME)
                    } },
                    onGoToTickets = { navController.navigate(Routes.MY_TICKETS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                    } },
                )
            }
            composable(
                route = Routes.RESALE_CREATE,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                ResaleCreateScreen(
                    ticketId = ticketId,
                    onSuccess = { navController.navigate(Routes.MY_TICKETS) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                    } },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Utility Screens ──
            composable(Routes.WISHLIST) {
                WishlistScreen(
                    onEventClick = { eventId -> navController.navigate(Routes.eventDetail(eventId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.MY_PAGE) {
                MyPageScreen(
                    onWallet = { navController.navigate(Routes.WALLET) },
                    onWishlist = { navController.navigate(Routes.WISHLIST) },
                    onWalletHistory = { navController.navigate(Routes.WALLET_HISTORY) },
                    onTxHistory = { navController.navigate(Routes.TX_HISTORY) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.WALLET) {
                WalletScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.WALLET_HISTORY) {
                WalletHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.TX_HISTORY) {
                TxHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.WITHDRAW) {
                WithdrawScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onPasswordChange = { navController.navigate(Routes.PASSWORD_CHANGE) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PASSWORD_CHANGE) {
                PasswordChangeScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.FIND_ACCOUNT) {
                FindAccountScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PASSWORD_RESET) {
                PasswordResetScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.RESALE_LIST) {
                ResaleListScreen(
                    onEventClick = { eventId -> navController.navigate(Routes.resaleTickets(eventId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.RESALE_TICKETS,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                ResaleTicketsScreen(
                    eventId = eventId,
                    onResaleItemClick = { resaleId -> navController.navigate(Routes.resaleDetail(resaleId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ARCHIVE) {
                ArchiveScreen(
                    onTicketClick = { ticketId ->
                        navController.navigate(Routes.collectibleDetail(ticketId))
                    },
                )
            }
            composable(
                route = Routes.COLLECTIBLE_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                CollectibleTicketDetailScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
