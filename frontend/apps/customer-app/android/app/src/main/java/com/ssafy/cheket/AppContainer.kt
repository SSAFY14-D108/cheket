package com.ssafy.cheket

import com.ssafy.cheket.core.network.AuthDataStore
import com.ssafy.cheket.core.network.RetrofitClient
import com.ssafy.cheket.core.network.service.*
import com.ssafy.cheket.core.repository.*
import com.ssafy.cheket.core.repository.fake.*
import com.ssafy.cheket.core.repository.impl.*

interface AppContainer {
    val authRepository: AuthRepository
    val showRepository: ShowRepository
    val ticketRepository: TicketRepository
    val resaleRepository: ResaleRepository
    val userRepository: UserRepository

    // Services (ViewModel에서 직접 필요할 때 사용)
    val showService: ShowService
    val ticketService: TicketService
    val queueService: QueueService
    val walletService: WalletService
    val authService: AuthService
    val resaleService: ResaleService
    val userService: UserService
}

/**
 * A: 실제 서버 연결 — Real Service + Real RepositoryImpl (API 호출)
 */
class RealAppContainer(
    private val authDataStore: AuthDataStore
) : AppContainer {
    override val showService: ShowService by lazy { RetrofitClient.createService(ShowService::class.java) }
    override val ticketService: TicketService by lazy { RetrofitClient.createService(TicketService::class.java) }
    override val queueService: QueueService by lazy { RetrofitClient.createService(QueueService::class.java) }
    override val walletService: WalletService by lazy { RetrofitClient.createService(WalletService::class.java) }
    override val authService: AuthService by lazy { RetrofitClient.createService(AuthService::class.java) }
    override val resaleService: ResaleService by lazy { RetrofitClient.createService(ResaleService::class.java) }
    override val userService: UserService by lazy { RetrofitClient.createService(UserService::class.java) }

    override val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authService, userService, authDataStore) }
    override val showRepository: ShowRepository by lazy { ShowRepositoryImpl(showService) }
    override val ticketRepository: TicketRepository by lazy { TicketRepositoryImpl(ticketService) }
    override val resaleRepository: ResaleRepository by lazy { ResaleRepositoryImpl(resaleService) }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl(userService) }
}

/**
 * B: MockInterceptor — Mock Service (HTTP 레벨 모킹) + Real RepositoryImpl (API 호출)
 */
class MockAppContainer(
    private val authDataStore: AuthDataStore
) : AppContainer {
    override val showService: ShowService by lazy { RetrofitClient.createMockService(ShowService::class.java) }
    override val ticketService: TicketService by lazy { RetrofitClient.createMockService(TicketService::class.java) }
    override val queueService: QueueService by lazy { RetrofitClient.createMockService(QueueService::class.java) }
    override val walletService: WalletService by lazy { RetrofitClient.createMockService(WalletService::class.java) }
    override val authService: AuthService by lazy { RetrofitClient.createMockService(AuthService::class.java) }
    override val resaleService: ResaleService by lazy { RetrofitClient.createMockService(ResaleService::class.java) }
    override val userService: UserService by lazy { RetrofitClient.createMockService(UserService::class.java) }

    override val authRepository: AuthRepository by lazy { AuthRepositoryImpl(authService, userService, authDataStore) }
    override val showRepository: ShowRepository by lazy { ShowRepositoryImpl(showService) }
    override val ticketRepository: TicketRepository by lazy { TicketRepositoryImpl(ticketService) }
    override val resaleRepository: ResaleRepository by lazy { ResaleRepositoryImpl(resaleService) }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl(userService) }
}

/**
 * C: FakeRepository — MockDataSource 직접 사용 (Service 무관)
 */
class FakeAppContainer : AppContainer {
    override val showService: ShowService by lazy { RetrofitClient.createMockService(ShowService::class.java) }
    override val ticketService: TicketService by lazy { RetrofitClient.createMockService(TicketService::class.java) }
    override val queueService: QueueService by lazy { RetrofitClient.createMockService(QueueService::class.java) }
    override val walletService: WalletService by lazy { RetrofitClient.createMockService(WalletService::class.java) }
    override val authService: AuthService by lazy { RetrofitClient.createMockService(AuthService::class.java) }
    override val resaleService: ResaleService by lazy { RetrofitClient.createMockService(ResaleService::class.java) }
    override val userService: UserService by lazy { RetrofitClient.createMockService(UserService::class.java) }

    override val authRepository: AuthRepository by lazy { FakeAuthRepository() }
    override val showRepository: ShowRepository by lazy { FakeShowRepository() }
    override val ticketRepository: TicketRepository by lazy { FakeTicketRepository() }
    override val resaleRepository: ResaleRepository by lazy { FakeResaleRepository() }
    override val userRepository: UserRepository by lazy { FakeUserRepository() }
}
