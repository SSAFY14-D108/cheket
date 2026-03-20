package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.model.ResaleItem
import com.ssafy.cheket.core.network.service.ResaleService
import com.ssafy.cheket.core.repository.ResaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "ResaleRepositoryImpl"
private const val RESALE_PRICE_FETCH_SIZE = 100

class ResaleRepositoryImpl(
    private val resaleService: ResaleService,
) : ResaleRepository {

    override fun getResaleItems(): Flow<List<ResaleItem>> = flow {
        Log.d(TAG, "getResaleItems()")
        try {
            val response = resaleService.getResaleShows()
            Log.d(TAG, "getResaleItems() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val items = response.data?.shows?.map { dto ->
                ResaleItem(
                    id = dto.showId.toString(),
                    ticketId = "",
                    showId = dto.showId.toString(),
                    showName = dto.title,
                    showDate = dto.showStartDate,
                    venue = dto.venue,
                    poster = dto.posterUrl,
                    seatLabel = "",
                    grade = "",
                    originalPrice = 0,
                    resalePrice = 0,
                    sellerId = "",
                )
            } ?: emptyList()
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "getResaleItems() error", e)
            emit(emptyList())
        }
    }

    override fun getResaleGrouped(): Flow<List<ResaleGroupItem>> = flow {
        Log.d(TAG, "getResaleGrouped()")
        try {
            val response = resaleService.getResaleShows()
            Log.d(TAG, "getResaleGrouped() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val groups = response.data?.shows?.map { dto ->
                val minPrice = fetchMinResalePrice(dto.showId)
                ResaleGroupItem(
                    showId = dto.showId.toString(),
                    showName = dto.title,
                    poster = dto.posterUrl,
                    venue = dto.venue,
                    region = dto.region,
                    showDate = dto.showStartDate,
                    count = dto.ticketCount,
                    minPrice = minPrice,
                    items = emptyList(),
                )
            } ?: emptyList()
            emit(groups)
        } catch (e: Exception) {
            Log.e(TAG, "getResaleGrouped() error", e)
            emit(emptyList())
        }
    }

    private suspend fun fetchMinResalePrice(showId: Long): Int {
        return try {
            val response = resaleService.getResaleTickets(showId = showId, size = RESALE_PRICE_FETCH_SIZE)
            response.data?.tickets?.minOfOrNull { it.discountedPrice } ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "fetchMinResalePrice() failed for showId=$showId", e)
            0
        }
    }
}
