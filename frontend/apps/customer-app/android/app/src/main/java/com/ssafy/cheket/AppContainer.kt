package com.ssafy.cheket

import com.ssafy.cheket.core.repository.*
import com.ssafy.cheket.core.repository.impl.*

interface AppContainer {
    val authRepository: AuthRepository
    val eventRepository: EventRepository
    val ticketRepository: TicketRepository
    val resaleRepository: ResaleRepository
    val userRepository: UserRepository
}

class RealAppContainer : AppContainer {
    override val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
    override val eventRepository: EventRepository by lazy { EventRepositoryImpl() }
    override val ticketRepository: TicketRepository by lazy { TicketRepositoryImpl() }
    override val resaleRepository: ResaleRepository by lazy { ResaleRepositoryImpl() }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl() }
}
