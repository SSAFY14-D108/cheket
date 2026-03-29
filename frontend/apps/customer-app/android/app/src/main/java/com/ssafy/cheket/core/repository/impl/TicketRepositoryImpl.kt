package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.OnchainInfo
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.network.dto.CollectionTicketDto
import com.ssafy.cheket.core.network.dto.OnchainInfoDto
import com.ssafy.cheket.core.network.dto.TicketShowDto
import com.ssafy.cheket.core.network.dto.UpcomingTicketDto
import com.ssafy.cheket.core.network.service.TicketService
import com.ssafy.cheket.core.repository.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "TicketRepositoryImpl"

class TicketRepositoryImpl(
    private val ticketService: TicketService,
) : TicketRepository {

    private var cachedUpcomingTickets: List<Ticket> = emptyList()
    private var cachedCollectionTickets: List<Ticket> = emptyList()

    override fun getTickets(): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getTickets()")
        try {
            val response = ticketService.getUpcomingTickets()
            Log.d(TAG, "getTickets() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            val tickets = response.data?.map(::mapUpcomingTicket) ?: emptyList()
            cachedUpcomingTickets = tickets
            emit(tickets)
        } catch (e: Exception) {
            Log.e(TAG, "getTickets() error", e)
            throw e
        }
    }

    override fun getTicketsByStatus(status: TicketStatus): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getTicketsByStatus() status=$status")
        try {
            val response = ticketService.getUpcomingTickets()
            val allTickets = response.data?.map(::mapUpcomingTicket) ?: emptyList()
            cachedUpcomingTickets = allTickets
            emit(allTickets.filter { it.status == status })
        } catch (e: Exception) {
            Log.e(TAG, "getTicketsByStatus() error", e)
            emit(emptyList())
        }
    }

    override fun getUsedTickets(): Flow<List<Ticket>> = flow {
        Log.d(TAG, "getUsedTickets()")
        try {
            val tickets = fetchCollectionTickets()
            cachedCollectionTickets = tickets
            emit(tickets)
        } catch (e: Exception) {
            Log.e(TAG, "getUsedTickets() error", e)
            emit(emptyList())
        }
    }

    override suspend fun getTicketById(id: String): Ticket? {
        Log.d(TAG, "getTicketById() id=$id")

        cachedUpcomingTickets.find { it.id == id }?.let { return it }
        cachedCollectionTickets.find { it.id == id }?.let { return it }

        return try {
            val response = ticketService.getUpcomingTickets()
            val tickets = response.data?.map(::mapUpcomingTicket) ?: emptyList()
            cachedUpcomingTickets = tickets
            tickets.find { it.id == id } ?: fetchCollectionTickets().also {
                cachedCollectionTickets = it
            }.find { it.id == id }
        } catch (e: Exception) {
            Log.e(TAG, "getTicketById() error", e)
            null
        }
    }

    private suspend fun fetchCollectionTickets(): List<Ticket> {
        return try {
            val response = ticketService.getCollectionTicketsOnchain()
            Log.d(TAG, "getUsedTickets(onchain) statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            response.data?.map(::mapCollectionTicket) ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "getUsedTickets(onchain) fallback to legacy collection endpoint", e)
            val response = ticketService.getCollectionTickets()
            Log.d(TAG, "getUsedTickets(legacy) statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            response.data?.map(::mapCollectionTicket) ?: emptyList()
        }
    }

    private fun mapUpcomingTicket(dto: UpcomingTicketDto): Ticket {
        return Ticket(
            id = dto.ticketId.toString(),
            numbering = dto.numbering,
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
            resalePrice = dto.resalePrice,
            metadataIpfsCid = dto.metadataIpfsCid,
            posterIpfsCid = dto.posterIpfsCid,
            effect = dto.show.effect,
        )
    }

    private fun mapCollectionTicket(dto: CollectionTicketDto): Ticket {
        val show = dto.show ?: dto.showInfo ?: TicketShowDto(
            showId = 0L,
            name = "",
            date = "",
            venue = "",
        )
        val tokenUri = dto.onchain?.tokenUri ?: dto.metadataIpfsCid
        val collectionStatus = when ((dto.status ?: dto.onchain?.onchainStatus).orEmpty().uppercase()) {
            "VALID" -> TicketStatus.AVAILABLE
            "EXPIRED" -> TicketStatus.EXPIRED
            "REFUNDED" -> TicketStatus.EXPIRED
            else -> TicketStatus.USED
        }

        return Ticket(
            id = dto.ticketId.toString(),
            numbering = dto.numbering,
            showId = show.showId.toString(),
            showName = show.name,
            showDate = show.date,
            venue = show.venue,
            poster = dto.posterUrl,
            seatId = dto.seatId?.toString() ?: "",
            seatLabel = "${dto.sectionName} ${dto.seatNo}",
            grade = dto.grade,
            originalPrice = dto.price ?: dto.onchain?.price?.toInt() ?: 0,
            status = collectionStatus,
            resalePrice = dto.resalePrice,
            metadataIpfsCid = dto.metadataIpfsCid,
            tokenId = dto.onchain?.tokenId?.toString(),
            ownerAddress = dto.onchain?.ownerAddress,
            tokenUri = tokenUri,
            posterIpfsCid = dto.posterIpfsCid,
            onchainStatus = dto.onchain?.onchainStatus,
            mintedAt = dto.onchain?.mintedAt,
            effect = show.effect,
            onchain = dto.onchain?.let(::mapOnchainInfo),
        )
    }

    private fun mapOnchainInfo(dto: OnchainInfoDto): OnchainInfo = OnchainInfo(
        tokenId = dto.tokenId ?: 0L,
        ownerAddress = dto.ownerAddress.orEmpty(),
        tokenURI = dto.tokenUri.orEmpty(),
        onchainStatus = dto.onchainStatus.orEmpty(),
        price = dto.price ?: 0L,
        mintedAt = dto.mintedAt ?: 0L,
    )

    private fun mapTicketStatus(status: String): TicketStatus = when (status.uppercase()) {
        "AVAILABLE" -> TicketStatus.AVAILABLE
        "SOLD" -> TicketStatus.SOLD
        "LISTED" -> TicketStatus.LISTED
        "ON-SALE" -> TicketStatus.LISTED
        "ON_SALE" -> TicketStatus.LISTED
        "USED" -> TicketStatus.USED
        "EXPIRED" -> TicketStatus.EXPIRED
        else -> TicketStatus.AVAILABLE
    }
}
