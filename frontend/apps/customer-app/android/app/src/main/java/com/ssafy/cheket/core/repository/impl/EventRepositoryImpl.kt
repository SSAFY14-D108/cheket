package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "EventRepositoryImpl"

class EventRepositoryImpl(
    private val showService: ShowService,
) : EventRepository {

    override fun getEvents(): Flow<List<Event>> = flow {
        Log.d(TAG, "getEvents()")
        try {
            val response = showService.getShows()
            Log.d(TAG, "getEvents() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val events = response.data?.shows?.map { dto ->
                Event(
                    id = dto.showId.toString(),
                    name = dto.title,
                    date = dto.showStartDate,
                    venue = dto.venue,
                    region = dto.region,
                    poster = dto.posterUrl,
                    status = if (dto.showStatus == "SOLD_OUT") EventStatus.SOLD_OUT else EventStatus.ON_SALE,
                    maxPerUser = 4,
                    grades = emptyList(),
                )
            } ?: emptyList()
            emit(events)
        } catch (e: Exception) {
            Log.e(TAG, "getEvents() error", e)
            emit(emptyList())
        }
    }

    override fun getBannerSlides(): Flow<List<BannerSlide>> = flow {
        Log.d(TAG, "getBannerSlides()")
        // TODO: 배너 슬라이드 전용 API 없음
        emit(emptyList())
    }

    override fun getCategories(): Flow<List<CategoryIcon>> = flow {
        Log.d(TAG, "getCategories()")
        // TODO: 카테고리 전용 API 없음
        emit(emptyList())
    }

    override fun getRankingItems(): Flow<List<RankingItem>> = flow {
        Log.d(TAG, "getRankingItems()")
        // TODO: 랭킹 전용 API 없음 (sort=popular 로 대체 가능)
        emit(emptyList())
    }

    override fun getOpenSchedule(): Flow<List<OpenScheduleItem>> = flow {
        Log.d(TAG, "getOpenSchedule()")
        try {
            val response = showService.getUpcomingShows()
            Log.d(TAG, "getOpenSchedule() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            val items = response.data?.map { dto ->
                OpenScheduleItem(
                    id = dto.showId.toString(),
                    eventId = dto.showId.toString(),
                    name = dto.title,
                    openLabel = dto.reservationDate,
                    openType = dto.status,
                    tags = emptyList(),
                    poster = dto.posterUrl,
                    isToday = false,
                )
            } ?: emptyList()
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "getOpenSchedule() error", e)
            emit(emptyList())
        }
    }

    override fun getDiscountItems(): Flow<List<DiscountItem>> = flow {
        Log.d(TAG, "getDiscountItems()")
        // TODO: 할인 전용 API 없음
        emit(emptyList())
    }

    override suspend fun getEventById(id: String): Event? {
        Log.d(TAG, "getEventById() id=$id")
        return try {
            val response = showService.getShowDetail(id.toLong())
            Log.d(TAG, "getEventById() statusCode=${response.httpStatusCode}")
            response.data?.let { dto ->
                Event(
                    id = dto.showId.toString(),
                    name = dto.title,
                    artistName = dto.artist,
                    date = dto.showStartDate,
                    venue = dto.venue,
                    region = dto.region,
                    poster = dto.posterUrl,
                    status = if (dto.showStatus == "SOLD_OUT") EventStatus.SOLD_OUT else EventStatus.ON_SALE,
                    maxPerUser = 4,
                    grades = dto.grade.map { g ->
                        Grade(name = g.gradeName, price = g.price, remaining = 0)
                    },
                    description = dto.description,
                    refundRules = dto.refundPolicy?.map { r ->
                        RefundRule(
                            id = "rule_${r.daysRemaining}",
                            daysBefore = r.daysRemaining,
                            feeRate = (100 - r.refundRate) / 100f,
                            label = "${r.daysRemaining}일 전: ${r.refundRate}% 환불",
                        )
                    } ?: emptyList(),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getEventById() error", e)
            null
        }
    }
}
