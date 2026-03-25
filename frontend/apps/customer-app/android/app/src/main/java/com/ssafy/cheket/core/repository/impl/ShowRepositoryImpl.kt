package com.ssafy.cheket.core.repository.impl

import android.util.Log
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.network.service.ShowService
import com.ssafy.cheket.core.repository.ShowPage
import com.ssafy.cheket.core.repository.ShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "ShowRepositoryImpl"

class ShowRepositoryImpl(
    private val showService: ShowService,
) : ShowRepository {

    private fun mapShowStatus(
        status: String?,
        showStartDate: String? = null,
        showEndDate: String? = null,
        reservationStartDate: String? = null,
        reservationEndDate: String? = null,
    ): ShowStatus {
        if (status == "SOLD_OUT") return ShowStatus.SOLD_OUT
        if (status == "COMPLETED") return ShowStatus.COMPLETED

        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val reservationStart = reservationStartDate.parseDateTimeOrNull()
        val reservationEnd = reservationEndDate.parseDateTimeOrNull()
        val showStart = showStartDate.parseDateOrNull()
        val showEnd = showEndDate.parseDateOrNull() ?: showStart

        if (showEnd != null && showEnd.isBefore(today)) return ShowStatus.COMPLETED
        if (reservationStart != null && now.isBefore(reservationStart)) return ShowStatus.UPCOMING
        if (reservationEnd != null && now.isAfter(reservationEnd)) {
            return if (showEnd != null && !showEnd.isBefore(today)) {
                ShowStatus.SOLD_OUT
            } else {
                ShowStatus.COMPLETED
            }
        }

        return if (status == "UPCOMING") ShowStatus.UPCOMING else ShowStatus.ON_SALE
    }

    private fun mapSummaryToShow(dto: com.ssafy.cheket.core.network.dto.ShowSummaryDto): Show =
        Show(
            id = dto.showId.toString(),
            name = dto.title,
            date = dto.show?.showStartDate ?: "",
            venue = dto.venue,
            region = dto.region,
            poster = dto.posterUrl,
            status = mapShowStatus(
                status = dto.status,
                showStartDate = dto.show?.showStartDate,
                showEndDate = dto.show?.showEndDate,
                reservationStartDate = dto.reservation?.startDate,
                reservationEndDate = dto.reservation?.endDate,
            ),
            maxPerUser = dto.purchaseLimit ?: 4,
            grades = emptyList(),
            openDate = dto.reservation?.startDate,
        )

    override suspend fun getShowsPage(
        regions: List<Int>?,
        sort: String?,
        keyword: String?,
        page: Int,
        size: Int,
    ): ShowPage {
        Log.d(TAG, "getShowsPage(regions=$regions, sort=$sort, keyword=$keyword, page=$page, size=$size)")
        return try {
            val response = showService.getShows(
                regions = regions,
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
        val slides = try {
            val response = showService.getRecommendations(size = 5, excludeLiked = false)
            Log.d(TAG, "getBannerSlides() recommendations statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val recommended = response.data?.shows?.map(::mapRecommendationToBannerSlide).orEmpty()
            if (recommended.isNotEmpty()) {
                recommended
            } else {
                buildRankingBannerSlides()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBannerSlides() recommendations failed, falling back to ranking", e)
            buildRankingBannerSlides()
        }
        emit(slides)
    }

    override fun getCategories(): Flow<List<CategoryIcon>> = flow {
        Log.d(TAG, "getCategories()")
        // TODO: 카테고리 전용 API 없음
        emit(emptyList())
    }

    override fun getRankingItems(): Flow<List<RankingItem>> = flow {
        Log.d(TAG, "getRankingItems()")
        try {
            val response = showService.getShows(sort = "POPULAR", page = 0, size = 5)
            Log.d(TAG, "getRankingItems() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val items = response.data?.shows?.mapIndexed { index, dto ->
                RankingItem(
                    rank = index + 1,
                    showId = dto.showId.toString(),
                    name = dto.title,
                    venue = dto.venue,
                    poster = dto.posterUrl,
                    genre = dto.region,
                )
            } ?: emptyList()
            emit(items)
        } catch (e: Exception) {
            Log.e(TAG, "getRankingItems() error", e)
            emit(emptyList())
        }
    }

    override fun getOpenSchedule(): Flow<List<OpenScheduleItem>> = flow {
        Log.d(TAG, "getOpenSchedule()")
        try {
            val response = showService.getUpcomingShows()
            Log.d(TAG, "getOpenSchedule() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
            val items = response.data?.shows?.map { dto ->
                val openDateStr = dto.reservation?.startDate ?: ""
                val openLabel = formatOpenLabel(openDateStr)
                OpenScheduleItem(
                    id = dto.showId.toString(),
                    showId = dto.showId.toString(),
                    name = dto.title,
                    openLabel = openLabel,
                    openType = dto.venue,
                    tags = listOfNotNull(dto.region.takeIf { it.isNotBlank() }),
                    poster = dto.posterUrl,
                    isToday = false,
                    venue = dto.venue,
                    region = dto.region,
                    showDate = dto.show?.showStartDate ?: "",
                    showEndDate = dto.show?.showEndDate ?: "",
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
                    status = mapShowStatus(
                        status = dto.status,
                        showStartDate = dto.show?.showStartDate,
                        showEndDate = dto.show?.showEndDate,
                        reservationStartDate = dto.reservation?.startDate,
                        reservationEndDate = dto.reservation?.endDate,
                    ),
                    maxPerUser = dto.purchaseLimit ?: 4,
                    openDate = dto.reservation?.startDate,
                    reservationEndDate = dto.reservation?.endDate,
                    grades = dto.grade.map { g ->
                        Grade(
                            name = g.gradeName,
                            price = g.price,
                            remaining = null,  // 상세 API에서 잔여석 정보 없음
                            color = g.colorCode,
                        )
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
                    descriptionImages = dto.descriptionImages ?: emptyList(),
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

    override suspend fun getLikedShows(): List<LikedShow> {
        Log.d(TAG, "getLikedShows()")
        return try {
            val response = showService.getLikedShows()
            Log.d(TAG, "getLikedShows() statusCode=${response.httpStatusCode}, count=${response.data?.size}")
            response.data?.map { dto ->
                LikedShow(
                    showId = dto.showId.toString(),
                    title = dto.title,
                    posterUrl = dto.posterUrl,
                    venue = dto.venue,
                    showStartDate = dto.showDate,
                    status = dto.status,
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "getLikedShows() error", e)
            emptyList()
        }
    }

    private suspend fun buildRankingBannerSlides(): List<BannerSlide> {
        val response = showService.getShows(sort = "POPULAR", page = 0, size = 5)
        Log.d(TAG, "buildRankingBannerSlides() statusCode=${response.httpStatusCode}, count=${response.data?.shows?.size}")
        return response.data?.shows?.mapIndexed { index, dto ->
            BannerSlide(
                id = "ranking_${dto.showId}",
                showId = dto.showId.toString(),
                image = dto.posterUrl,
                title = dto.title,
                subtitle = "랭킹 ${index + 1}위 공연",
                venue = dto.venue,
                dates = buildBannerDateLabel(dto.show?.showStartDate, dto.show?.showEndDate),
            )
        }.orEmpty()
    }

    private fun mapRecommendationToBannerSlide(dto: com.ssafy.cheket.core.network.dto.RecommendedShowDto): BannerSlide =
        BannerSlide(
            id = "recommendation_${dto.showId}",
            showId = dto.showId.toString(),
            image = dto.posterUrl,
            title = dto.title,
            subtitle = dto.reason ?: "AI가 추천한 공연",
            venue = buildBannerVenueLabel(dto.artist, dto.venue),
            dates = buildBannerDateLabel(dto.show?.showStartDate, dto.show?.showEndDate),
        )

    private fun buildBannerVenueLabel(artist: String?, venue: String): String {
        return if (!artist.isNullOrBlank()) {
            "$artist · $venue"
        } else {
            venue
        }
    }

    private fun String?.parseDateTimeOrNull(): LocalDateTime? {
        if (this.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: Exception) {
            null
        }
    }

    private fun String?.parseDateOrNull(): LocalDate? {
        if (this.isNullOrBlank()) return null
        return try {
            LocalDate.parse(this)
        } catch (_: Exception) {
            null
        }
    }

    private fun buildBannerDateLabel(startDate: String?, endDate: String?): String {
        if (startDate.isNullOrBlank()) return ""
        return if (!endDate.isNullOrBlank() && endDate != startDate) {
            "$startDate ~ $endDate"
        } else {
            startDate
        }
    }

    /** "2026-04-01T20:00:00" → "4/1 (화) 20:00 오픈" */
    private fun formatOpenLabel(dateStr: String): String {
        if (dateStr.isBlank()) return ""
        return try {
            val dt = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val dayOfWeek = dt.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)
            "${dt.monthValue}/${dt.dayOfMonth} ($dayOfWeek) %02d:%02d 오픈".format(dt.hour, dt.minute)
        } catch (_: Exception) {
            dateStr
        }
    }
}
