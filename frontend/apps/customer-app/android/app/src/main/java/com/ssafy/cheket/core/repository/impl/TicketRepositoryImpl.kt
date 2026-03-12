package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.network.service.TicketService
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "TicketRepositoryImpl"

class TicketRepositoryImpl(
    private val ticketService: TicketService,
) : TicketRepository {

    override fun getTickets(): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getTickets()")
        try {
            val response = ticketService.getUpcomingTickets()
            Log.d(TAG, "getTickets() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            val tickets = response.data?.map { dto ->
                Ticket(
                    id = dto.ticketId.toString(),
                    showId = dto.show.showId.toString(),
                    showName = dto.show.name,
                    showDate = dto.show.date,
                    venue = dto.show.venue,
                    poster = dto.posterUrl,
                    seatId = dto.seatId.toString(),
                    seatLabel = "${dto.sectionName} ${dto.seatNo}",
                    grade = dto.grade,
                    originalPrice = dto.price,
                    status = mapTicketStatus(dto.status),
                )
            } ?: emptyList()
            emit(tickets)
        } catch (e: Exception) {
            Log.e(TAG, "getTickets() error", e)
            emit(emptyList())
        }
    }

    override fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getTicketsByStatus() status=$status")
        // TODO: 서버에 status 필터링 API 없음 — 클라이언트에서 필터
        try {
            val response = ticketService.getUpcomingTickets()
            val tickets = response.data?.map { dto ->
                Ticket(
                    id = dto.ticketId.toString(),
                    showId = dto.show.showId.toString(),
                    showName = dto.show.name,
                    showDate = dto.show.date,
                    venue = dto.show.venue,
                    poster = dto.posterUrl,
                    seatId = dto.seatId.toString(),
                    seatLabel = "${dto.sectionName} ${dto.seatNo}",
                    grade = dto.grade,
                    originalPrice = dto.price,
                    status = mapTicketStatus(dto.status),
                )
            }?.filter { it.status == status } ?: emptyList()
            emit(tickets)
        } catch (e: Exception) {
            Log.e(TAG, "getTicketsByStatus() error", e)
            emit(emptyList())
        }
    }

    override fun getUsedTickets(): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getUsedTickets()")
        try {
            val response = ticketService.getCollectionTickets()
            Log.d(TAG, "getUsedTickets() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            val tickets = response.data?.map { dto ->
                Ticket(
                    id = dto.ticketId.toString(),
                    showId = dto.show.showId.toString(),
                    showName = dto.show.name,
                    showDate = dto.show.date,
                    venue = dto.show.venue,
                    poster = dto.posterUrl,
                    seatId = "",
                    seatLabel = "${dto.sectionName} ${dto.seatNo}",
                    grade = dto.grade,
                    originalPrice = 0,
                    status = TicketStatus.USED,
                )
            } ?: emptyList()
            emit(tickets)
        } catch (e: Exception) {
            Log.e(TAG, "getUsedTickets() error", e)
            emit(emptyList())
        }
    }

    override suspend fun getTicketById(id: String): Ticket? {
        Log.d(TAG, "getTicketById() id=$id")
        // TODO: 개별 티켓 조회 API 없음
        return null
    }

    private fun mapTicketStatus(status: String): TicketStatus = when (status.uppercase()) {
        "SOLD" -> TicketStatus.SOLD
        "LISTED" -> TicketStatus.LISTED
        "USED" -> TicketStatus.USED
        "EXPIRED" -> TicketStatus.EXPIRED
        else -> TicketStatus.SOLD
    }
}
