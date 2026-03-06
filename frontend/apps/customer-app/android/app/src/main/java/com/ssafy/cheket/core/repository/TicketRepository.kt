package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getTickets(): Flow<List<Ticket>>
    fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>>
    fun getUsedTickets(): Flow<List<Ticket>>
    suspend fun getTicketById(id: String): Ticket?
}
