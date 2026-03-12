package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.repository.ShowPage
import com.ssafy.cheket.core.repository.ShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "ShowRepositoryImpl"

class ShowRepositoryImpl(
    private val showService: ShowService,
) : ShowRepository {

    private fun mapSummaryToShow(dto: com.ssafy.cheket.core.network.dto.ShowSummaryDto): Show =
        Show(
            id = dto.showId.toString(),
            name = dto.title,
            date = dto.show?.showStartDate ?: "",
            venue = dto.venue,
            region = dto.region,
            poster = dto.posterUrl,
            status = if (dto.status == "SOLD_OUT") ShowStatus.SOLD_OUT else ShowStatus.ON_SALE,
            maxPerUser = dto.purchaseLimit ?: 4,
            grades = emptyList(),
            openDate = dto.reservation?.startDate,
        )

    override suspend fun getShowsPage(
        region: String?,
        sort: String?,
        keyword: String?,
        page: Int,
        size: Int,
    ): ShowPage {
        Log.d(TAG, "getShowsPage(region=$region, sort=$sort, keyword=$keyword, page=$page, size=$size)")
        return try {
            val response = showService.getShows(
                region = region,
                sort = sort,
                keyword = keyword,
                page = page,
                size = size,
            )
            Log.d(TAG, "getShowsPage() statusCode=${response.httpStatusCode}, totalPages=${response.data?.totalPages}")
            val data = response.data
            ShowPage(
                shows = data?.shows?.map(::mapSummaryToShow) ?: emptyList(),
                page = data?.page ?: 0,
                totalPages = data?.totalPages ?: 0,
                totalElements = data?.totalElements ?: 0,
            )
        } catch (e: Exception) {
            Log.e(TAG, "getShowsPage() error", e)
            ShowPage(emptyList(), 0, 0, 0)
        }
    }

    override fun getShows(): Flow<List<Show>> = flow {
        Log.d(TAG, "getShows()")
        try {
            val response = showService.getShows()
            Log.d(TAG, "getShows() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val shows = response.data?.shows?.map(::mapSummaryToShow) ?: emptyList()
            emit(shows)
        } catch (e: Exception) {
            Log.e(TAG, "getShows() error", e)
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
            Log.d(TAG, "getOpenSchedule() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val items = response.data?.shows?.map { dto ->
                OpenScheduleItem(
                    id = dto.showId.toString(),
                    showId = dto.showId.toString(),
                    name = dto.title,
                    openLabel = dto.reservation?.startDate ?: "",
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

    override suspend fun getShowById(id: String): Show? {
        Log.d(TAG, "getShowById() id=$id")
        return try {
            val response = showService.getShowDetail(id.toLong())
            Log.d(TAG, "getShowById() statusCode=${response.httpStatusCode}")
            response.data?.let { dto ->
                Show(
                    id = dto.showId.toString(),
                    name = dto.title,
                    artistName = dto.artist,
                    date = dto.show?.showStartDate ?: "",
                    venue = dto.venue,
                    region = dto.region,
                    poster = dto.posterUrl,
                    status = if (dto.status == "SOLD_OUT") ShowStatus.SOLD_OUT else ShowStatus.ON_SALE,
                    maxPerUser = 4,
                    grades = dto.grade.map { g ->
                        Grade(name = g.gradeName, price = g.price, remaining = 0)
                    },
                    description = dto.description,
                    isLiked = dto.isLiked,
                    likeCount = dto.likeCount,
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
            Log.e(TAG, "getShowById() error", e)
            null
        }
    }

    override suspend fun likeShow(showId: String) {
        Log.d(TAG, "likeShow() showId=$showId")
        try {
            showService.likeShow(showId.toLong())
        } catch (e: Exception) {
            Log.e(TAG, "likeShow() error", e)
            throw e
        }
    }

    override suspend fun unlikeShow(showId: String) {
        Log.d(TAG, "unlikeShow() showId=$showId")
        try {
            showService.unlikeShow(showId.toLong())
        } catch (e: Exception) {
            Log.e(TAG, "unlikeShow() error", e)
            throw e
        }
    }
}
