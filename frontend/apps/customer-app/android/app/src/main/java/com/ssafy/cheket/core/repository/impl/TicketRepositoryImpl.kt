package com.ssafy.cheket.core.repository.impl

import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TicketRepositoryImpl : TicketRepository {
    override fun getTickets(): Flow<List<Ticket>> = flow { emit(MockDataSource.mockTickets) }
    override fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>> = flow {
        emit(MockDataSource.mockTickets.filter { it.status == status })
    }
    override fun getUsedTickets(): Flow<List<Ticket>> = flow {
        emit(MockDataSource.mockTickets.filter { it.status == TicketStatus.USED })
    }
    override suspend fun getTicketById(id: String): Ticket? =
        MockDataSource.mockTickets.find { it.id == id }
}
