package com.ssafy.cheket

import androidx.compose.foundation.layout.imePadding
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.CheketBottomBar
import com.ssafy.cheket.core.ui.component.NetworkStatusObserver
import com.ssafy.cheket.features.auth.LoginScreen
import com.ssafy.cheket.features.auth.SignupScreen
import com.ssafy.cheket.features.collection.ArchiveScreen
import com.ssafy.cheket.features.collection.CollectibleTicketDetailScreen
import com.ssafy.cheket.features.collection.CollectionListWebViewScreen
import com.ssafy.cheket.features.collection.CollectionScreen
import com.ssafy.cheket.features.shows.ShowsScreen
import com.ssafy.cheket.features.show.ShowDetailScreen
import com.ssafy.cheket.features.home.HomeScreen
import com.ssafy.cheket.features.mypage.MyPageScreen
import com.ssafy.cheket.features.mypage.MyShowsScreen
import com.ssafy.cheket.features.mypage.ShowRevenueScreen
import com.ssafy.cheket.features.mytickets.MyTicketsScreen
import com.ssafy.cheket.features.mytickets.QrCheckinScreen
import com.ssafy.cheket.features.mytickets.TicketDetailScreen
import com.ssafy.cheket.features.mytickets.TicketHistoryScreen
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
import com.ssafy.cheket.features.purchase.SeatMapScreen
import com.ssafy.cheket.features.purchase.TransactionProcessingScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.ssafy.cheket.features.notification.ContractApprovalScreen
import com.ssafy.cheket.features.notification.NotificationScreen
import com.ssafy.cheket.features.show.ShowDateSelectionScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SHOWS = "shows"
    const val RESALE = "resale"
    const val MY_TICKETS = "my_tickets"
    const val TICKET_HISTORY = "ticket_history"
    const val COLLECTION = "collection"

    // Detail / Flow screens
    const val SHOW_DETAIL = "show_detail/{showId}"
    const val SHOW_DATE_SELECTION = "show_date_selection/{showId}"
    const val WAITING_QUEUE = "waiting_queue/{showId}/{showDateId}"
    const val SEAT_SELECTION = "seat_selection/{showId}/{showDateId}"
    const val PAYMENT = "payment/{showId}"
    const val PURCHASE_FAILED = "purchase_failed/{showId}/{reason}"
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
    const val RESALE_TICKETS = "resale_tickets/{showId}"
    const val SEAT_MAP = "seat_map/{showId}/{sessionId}"
    const val TX_PROCESSING = "tx_processing/{txId}/{txType}"
    const val NOTIFICATIONS = "notifications"
    const val CONTRACT_APPROVAL = "contract_approval/{showId}/{requestType}"
    const val MY_SHOWS = "my_shows"
    const val SHOW_REVENUE = "show_revenue/{showId}"

    // Helper functions for building routes with args
    fun showDetail(showId: String) = "show_detail/$showId"
    fun showDateSelection(showId: String) = "show_date_selection/$showId"
    fun waitingQueue(showId: String, showDateId: String) = "waiting_queue/$showId/$showDateId"
    fun seatSelection(showId: String, showDateId: String) = "seat_selection/$showId/$showDateId"
    fun payment(showId: String) = "payment/$showId"
    fun purchaseFailed(showId: String, reason: String) = "purchase_failed/$showId/${java.net.URLEncoder.encode(reason, "UTF-8")}"
    fun ticketDetail(ticketId: String) = "ticket_detail/$ticketId"
    fun qrCheckin(ticketId: String) = "qr_checkin/$ticketId"
    fun transfer(ticketId: String) = "transfer/$ticketId"
    fun transferComplete(ticketId: String) = "transfer_complete/$ticketId"
    fun transferFailed(ticketId: String) = "transfer_failed/$ticketId"
    fun resaleDetail(resaleItemId: String) = "resale_detail/$resaleItemId"
    fun resalePurchaseComplete(ticketId: String) = "resale_purchase_complete/$ticketId"
    fun resaleCreate(ticketId: String) = "resale_create/$ticketId"
    fun collectibleDetail(ticketId: String) = "collectible_detail/$ticketId"
    fun resaleTickets(showId: String) = "resale_tickets/$showId"
    fun seatMap(showId: String, sessionId: String = "") = "seat_map/$showId/$sessionId"
    fun txProcessing(txId: Long = 0, txType: String = "TICKET_PURCHASE") = "tx_processing/$txId/$txType"
    fun contractApproval(showId: Long, requestType: String) = "contract_approval/$showId/$requestType"
    fun showRevenue(showId: Long) = "show_revenue/$showId"
}

val bottomTabRoutes = listOf(
    Routes.HOME, Routes.SHOWS, Routes.RESALE, Routes.MY_TICKETS, Routes.MY_PAGE,
)

private const val ANIM_DURATION = 300

/**
 * Slide-in composable: 화면 진입 시 오른쪽에서 슬라이드 인,
 * 뒤로 갈 때 오른쪽으로 슬라이드 아웃.
 * 엣지 스와이프 뒤로 가기 제스처도 자동 지원 (predictive back).
 */
private fun NavGraphBuilder.slideComposable(
    route: String,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(ANIM_DURATION),
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(ANIM_DURATION),
            )
        },
        content = content,
    )
}

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    startLoggedIn: Boolean = false,
    navController: NavHostController = rememberNavController(),
) {
    // 네트워크 상태 감시 — 연결 끊기면 다이얼로그 자동 표시
    NetworkStatusObserver()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var splashDone by remember { mutableStateOf(false) }
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showBottomBar = splashDone && currentRoute in bottomTabRoutes && !isKeyboardOpen

    // FCM 딥링크 처리 — 알림 탭 시 해당 화면으로 이동
    val activity = LocalContext.current as? MainActivity
    val pendingNotif = activity?.pendingNotification?.collectAsState()?.value
    LaunchedEffect(pendingNotif, splashDone) {
        if (pendingNotif != null && splashDone) {
            val dummyDto = com.ssafy.cheket.core.network.dto.NotificationDto(
                id = pendingNotif.notificationId ?: 0,
                message = "",
                type = pendingNotif.type,
                isRead = false,
                showId = pendingNotif.showId,
            )
            navigateForNotification(navController, dummyDto)
            activity.consumeNotification()
        }
    }

    // 401 강제 로그아웃 처리 — 토큰 만료/갱신 실패 시 로그인 화면으로 이동
    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.ssafy.cheket.core.network.AuthEventBus.events.collect { event ->
            when (event) {
                is com.ssafy.cheket.core.network.AuthEvent.ForceLogout -> {
                    // 로그인/스플래시/회원가입 화면에서는 무시
                    val route = currentRoute
                    if (route == Routes.LOGIN || route == Routes.SPLASH || route == Routes.SIGNUP) {
                        android.util.Log.d("AppNavGraph", "ForceLogout ignored on auth screen: $route")
                        return@collect
                    }
                    // USER_LOGOUT(사용자 직접 로그아웃)은 다이얼로그 없이 처리
                    if (event.reason == com.ssafy.cheket.core.network.AuthLogoutReason.USER_LOGOUT) {
                        android.util.Log.d("AppNavGraph", "ForceLogout: user logout, navigating to login")
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                        return@collect
                    }
                    android.util.Log.w("AppNavGraph", "ForceLogout received: reason=${event.reason}")
                    showSessionExpiredDialog = true
                }
            }
        }
    }

    if (showSessionExpiredDialog) {
        com.ssafy.cheket.core.ui.component.CheketAlertDialog(
            title = "로그인 만료",
            message = "로그인 세션이 만료되었습니다.\n다시 로그인해주세요.",
            confirmText = "로그인",
            onConfirm = {
                showSessionExpiredDialog = false
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                CheketBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .imePadding(),
        ) {
            // ── Splash ──
            composable(
                route = Routes.SPLASH,
                exitTransition = { fadeOut(animationSpec = tween(300)) },
            ) {
                val scope = rememberCoroutineScope()
                com.ssafy.cheket.features.splash.SplashScreen(
                    onSplashFinished = {
                        scope.launch {
                            val dest = if (startLoggedIn) {
                                // 토큰 유효성 확인 — 실패하면 로그인으로
                                try {
                                    appContainer.userService.getUserInfo()
                                    Routes.HOME
                                } catch (e: Exception) {
                                    android.util.Log.w("AppNavGraph", "Token validation failed, redirecting to login", e)
                                    (activity?.applicationContext as? CheketApplication)?.authDataStore?.clear()
                                    Routes.LOGIN
                                }
                            } else Routes.LOGIN

                            navController.navigate(dest) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                            kotlinx.coroutines.delay(350)
                            splashDone = true
                        }
                    },
                )
            }

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
            slideComposable(Routes.SIGNUP) {
                SignupScreen(
                    onSignupSuccess = {
                        navController.popBackStack(Routes.LOGIN, inclusive = false)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Main tabs (no slide animation — instant switch) ──
            composable(Routes.HOME) {
                HomeScreen(
                    appContainer = appContainer,
                    onShowClick = { showId ->
                        navController.navigate(Routes.showDetail(showId)) {
                            launchSingleTop = false
                            restoreState = false
                        }
                    },
                    onMyPage = { navController.navigate(Routes.MY_PAGE) },
                    onNotificationClick = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenSoon = {
                        NavParams.initialSortOption = "OPEN_SOON"
                        navController.navigate(Routes.SHOWS) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLikedShowsMore = { navController.navigate(Routes.WISHLIST) },
                    onResaleMore = {
                        navController.navigate(Routes.RESALE) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSeatMapTest = { showId -> navController.navigate(Routes.seatMap(showId)) },
                )
            }
            composable(Routes.SHOWS) {
                ShowsScreen(
                    appContainer = appContainer,
                    onShowClick = { showId -> navController.navigate(Routes.showDetail(showId)) },
                )
            }
            composable(Routes.RESALE) {
                ResaleScreen(
                    appContainer = appContainer,
                    onResaleItemClick = { showId -> navController.navigate(Routes.resaleTickets(showId)) },
                )
            }
            composable(Routes.MY_TICKETS) {
                MyTicketsScreen(
                    appContainer = appContainer,
                    onTicketClick = { ticket ->
                        NavParams.selectedTicket = ticket
                        navController.navigate(Routes.ticketDetail(ticket.id))
                    },
                    onCollection = { navController.navigate(Routes.COLLECTION) },
                )
            }
            composable(Routes.COLLECTION) {
                CollectionListWebViewScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Show Detail ──
            slideComposable(
                route = Routes.SHOW_DETAIL,
                arguments = listOf(navArgument("showId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                ShowDetailScreen(
                    showId = showId,
                    onNavigateToDateSelection = { navController.navigate(Routes.showDateSelection(it)) },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Purchase Flow ──
            slideComposable(
                route = Routes.SHOW_DATE_SELECTION,
                arguments = listOf(navArgument("showId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                ShowDateSelectionScreen(
                    showId = showId,
                    onDateSelected = { sId, dateId, showName, showDateLabel ->
                        NavParams.waitingShowName = showName
                        NavParams.waitingShowDate = showDateLabel
                        navController.navigate(Routes.waitingQueue(sId, dateId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.WAITING_QUEUE,
                arguments = listOf(
                    navArgument("showId") { type = NavType.StringType },
                    navArgument("showDateId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                val showDateId = backStackEntry.arguments?.getString("showDateId") ?: ""
                WaitingQueueScreen(
                    showId = showId,
                    sessionId = showDateId,
                    showName = NavParams.waitingShowName,
                    showDate = NavParams.waitingShowDate,
                    queueService = appContainer.queueService,
                    onComplete = { navController.navigate(Routes.seatSelection(it, showDateId)) {
                        popUpTo(Routes.waitingQueue(showId, showDateId)) { inclusive = true }
                    } },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.SEAT_SELECTION,
                arguments = listOf(
                    navArgument("showId") { type = NavType.StringType },
                    navArgument("showDateId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                val sessionId = backStackEntry.arguments?.getString("showDateId") ?: ""
                SeatMapScreen(
                    showId = showId,
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onPurchase = { navController.navigate(Routes.payment(showId)) },
                )
            }
            slideComposable(
                route = Routes.PAYMENT,
                arguments = listOf(navArgument("showId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                PaymentScreen(
                    showId = showId,
                    onSuccess = { txId ->
                        // 결제 성공 → 블록체인 TX 처리 화면으로 이동
                        navController.navigate(Routes.txProcessing(txId = txId)) {
                            popUpTo(Routes.payment(showId)) { inclusive = true }
                        }
                    },
                    onFailure = { sId, reason ->
                        navController.navigate(Routes.purchaseFailed(sId, reason)) {
                            popUpTo(Routes.showDetail(sId)) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onBackToShowDetail = {
                        navController.navigate(Routes.showDetail(showId)) {
                            popUpTo(Routes.showDetail(showId)) { inclusive = true }
                        }
                    },
                )
            }
            // ── TX Processing ──
            slideComposable(
                route = Routes.TX_PROCESSING,
                arguments = listOf(
                    navArgument("txId") { type = NavType.StringType; defaultValue = "0" },
                    navArgument("txType") { type = NavType.StringType; defaultValue = "TICKET_PURCHASE" },
                ),
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("txId")?.toLongOrNull() ?: 0L
                val txType = backStackEntry.arguments?.getString("txType") ?: "TICKET_PURCHASE"
                TransactionProcessingScreen(
                    txId = txId,
                    txType = txType,
                    onComplete = {
                        navController.navigate(Routes.MY_TICKETS) {
                            popUpTo(Routes.HOME) {
                                inclusive = false
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    onFailure = { reason ->
                        navController.navigate(Routes.purchaseFailed("0", reason)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.PURCHASE_FAILED,
                arguments = listOf(
                    navArgument("showId") { type = NavType.StringType },
                    navArgument("reason") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                val reason = try {
                    java.net.URLDecoder.decode(backStackEntry.arguments?.getString("reason") ?: "", "UTF-8")
                } catch (_: Exception) { backStackEntry.arguments?.getString("reason") ?: "" }
                PurchaseFailedScreen(
                    showId = showId,
                    reason = reason,
                    onRetry = { navController.navigate(Routes.showDetail(it)) {
                        popUpTo(Routes.HOME)
                    } },
                    onGoHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    } },
                )
            }

            // ── Ticket Detail & Actions ──
            slideComposable(
                route = Routes.TICKET_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TicketDetailScreen(
                    ticketId = ticketId,
                    onQrCheckin = { navController.navigate(Routes.qrCheckin(it)) },
                    onTransfer = { navController.navigate(Routes.transfer(it)) },
                    onResaleCreate = { navController.navigate(Routes.resaleCreate(it)) },
                    onResaleCancelRequested = { txId ->
                        navController.navigate(Routes.txProcessing(txId, "RESALE_CANCEL")) {
                            popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                        }
                    },
                    onRefundSuccess = { txId ->
                        if (txId != null && txId > 0) {
                            navController.navigate(Routes.txProcessing(txId, "REFUND")) {
                                popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.MY_TICKETS) {
                                popUpTo(Routes.MY_TICKETS) { inclusive = true }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
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
            slideComposable(
                route = Routes.TRANSFER,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TransferScreen(
                    ticketId = ticketId,
                    onTransferComplete = {
                        val txId = NavParams.transferTransactionId
                        if (txId > 0) {
                            navController.navigate(Routes.txProcessing(txId, "TRANSFER")) {
                                popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.transferComplete(it)) {
                                popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                            }
                        }
                    },
                    onTransferFailed = { navController.navigate(Routes.transferFailed(it)) {
                        popUpTo(Routes.ticketDetail(ticketId)) { inclusive = true }
                    } },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.TRANSFER_COMPLETE,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                TransferCompleteScreen(
                    ticketId = ticketId,
                    onGoToTickets = { navController.navigate(Routes.MY_TICKETS) {
                        popUpTo(Routes.MY_TICKETS) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    } },
                    onGoHome = { navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    } },
                )
            }
            slideComposable(
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
                        popUpTo(Routes.MY_TICKETS) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    } },
                )
            }

            // ── Resale Flow ──
            slideComposable(
                route = Routes.RESALE_DETAIL,
                arguments = listOf(navArgument("resaleItemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val resaleItemId = backStackEntry.arguments?.getString("resaleItemId") ?: ""
                ResaleDetailScreen(
                    resaleItemId = resaleItemId,
                    onPurchaseComplete = { txId ->
                        navController.navigate(Routes.txProcessing(txId, "RESALE_PURCHASE")) {
                            popUpTo(Routes.RESALE)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
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
            slideComposable(
                route = Routes.RESALE_CREATE,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                ResaleCreateScreen(
                    ticketId = ticketId,
                    onSuccess = { txId ->
                        if (txId != null && txId > 0) {
                            navController.navigate(Routes.txProcessing(txId, "RESALE_LIST")) {
                                popUpTo(Routes.resaleCreate(ticketId)) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.MY_TICKETS) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Utility Screens ──
            slideComposable(Routes.WISHLIST) {
                WishlistScreen(
                    onShowClick = { showId -> navController.navigate(Routes.showDetail(showId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.MY_PAGE) {
                MyPageScreen(
                    userService = appContainer.userService,
                    onWallet = { navController.navigate(Routes.WALLET) },
                    onMyTickets = {
                        navController.navigate(Routes.TICKET_HISTORY) {
                            launchSingleTop = true
                        }
                    },
                    onCollection = { navController.navigate(Routes.COLLECTION) },
                    onWishlist = { navController.navigate(Routes.WISHLIST) },
                    onWalletHistory = { navController.navigate(Routes.WALLET_HISTORY) },
                    onTxHistory = { navController.navigate(Routes.TX_HISTORY) },
                    onMyShows = { navController.navigate(Routes.MY_SHOWS) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onLogout = {
                        appContainer.authRepository.logout()
                    },
                    onWithdrawSuccess = {
                        appContainer.authRepository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.TICKET_HISTORY) {
                TicketHistoryScreen(
                    onBack = { navController.popBackStack() },
                    onTicketClick = { ticket ->
                        NavParams.selectedTicket = ticket
                        navController.navigate(Routes.ticketDetail(ticket.id))
                    },
                )
            }
            slideComposable(Routes.MY_SHOWS) {
                MyShowsScreen(
                    onShowClick = { showId -> navController.navigate(Routes.showRevenue(showId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.SHOW_REVENUE,
                arguments = listOf(navArgument("showId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getLong("showId") ?: 0L
                ShowRevenueScreen(
                    showId = showId,
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.WALLET) {
                WalletScreen(
                    onWalletHistory = { navController.navigate(Routes.WALLET_HISTORY) },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.WALLET_HISTORY) {
                WalletHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.TX_HISTORY) {
                WalletHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.WITHDRAW) {
                WithdrawScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.SETTINGS) {
                SettingsScreen(
                    userService = appContainer.userService,
                    onPasswordChange = { navController.navigate(Routes.PASSWORD_CHANGE) },
                    onWithdraw = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.PASSWORD_CHANGE) {
                PasswordChangeScreen(
                    authRepository = appContainer.authRepository,
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.FIND_ACCOUNT) {
                FindAccountScreen(
                    authService = appContainer.authService,
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.PASSWORD_RESET) {
                PasswordResetScreen(
                    authService = appContainer.authService,
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(Routes.RESALE_LIST) {
                ResaleListScreen(
                    onShowClick = { showId -> navController.navigate(Routes.resaleTickets(showId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.RESALE_TICKETS,
                arguments = listOf(navArgument("showId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                ResaleTicketsScreen(
                    showId = showId,
                    onResaleItemClick = { resaleId -> navController.navigate(Routes.resaleDetail(resaleId)) },
                    onTxProcessing = { txId, txType ->
                        navController.navigate(Routes.txProcessing(txId, txType))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            // ── Seat Map ──
            slideComposable(
                route = Routes.SEAT_MAP,
                arguments = listOf(
                    navArgument("showId") { type = NavType.StringType },
                    navArgument("sessionId") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId") ?: ""
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                SeatMapScreen(
                    showId = showId,
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                    onPurchase = {
                        navController.navigate(Routes.payment(showId))
                    },
                )
            }

            slideComposable(Routes.ARCHIVE) {
                ArchiveScreen(
                    onTicketClick = { ticket ->
                        NavParams.selectedTicket = ticket
                        navController.navigate(Routes.collectibleDetail(ticket.id))
                    },
                )
            }
            slideComposable(
                route = Routes.COLLECTIBLE_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                CollectibleTicketDetailScreen(
                    ticketId = ticketId,
                    onBack = { navController.popBackStack() },
                )
            }

            // ── Notifications ──
            slideComposable(Routes.NOTIFICATIONS) {
                NotificationScreen(
                    onNotificationClick = { notification ->
                        navigateForNotification(navController, notification)
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            slideComposable(
                route = Routes.CONTRACT_APPROVAL,
                arguments = listOf(
                    navArgument("showId") { type = NavType.LongType },
                    navArgument("requestType") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val showId = backStackEntry.arguments?.getLong("showId") ?: 0L
                val requestType = backStackEntry.arguments?.getString("requestType") ?: "RQ_CREATE"
                ContractApprovalScreen(
                    showId = showId,
                    requestType = requestType,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * 알림 타입별 대응 화면으로 네비게이션.
 *
 * | 타입 | 대응 화면 |
 * |------|----------|
 * | SHOW_START | 내 티켓 목록 |
 * | SETTLEMENT | 지갑 내역 |
 * | APPROVED | 공연 상세 (showId) |
 * | REJECTED | 공연 상세 (showId) |
 * | RESALE | 내 티켓 목록 |
 * | RQ_CREATE | 계약 승인/거절 화면 |
 * | RQ_UPDATE | 계약 승인/거절 화면 |
 */
private fun navigateForNotification(
    navController: androidx.navigation.NavController,
    notification: com.ssafy.cheket.core.network.dto.NotificationDto,
) {
    when (notification.type) {
        "SHOW_START" -> navController.navigate(Routes.MY_TICKETS)
        "SETTLEMENT" -> navController.navigate(Routes.WALLET_HISTORY)
        "APPROVED", "REJECTED" -> {
            val showId = notification.showId
            if (showId != null) {
                navController.navigate(Routes.showDetail(showId.toString()))
            } else {
                navController.navigate(Routes.HOME)
            }
        }
        "RESALE" -> navController.navigate(Routes.MY_TICKETS)
        "RQ_CREATE" -> {
            val showId = notification.showId ?: 0L
            navController.navigate(Routes.contractApproval(showId, "RQ_CREATE"))
        }
        "RQ_UPDATE" -> {
            val showId = notification.showId ?: 0L
            navController.navigate(Routes.contractApproval(showId, "RQ_UPDATE"))
        }
        else -> navController.navigate(Routes.HOME)
    }
}
