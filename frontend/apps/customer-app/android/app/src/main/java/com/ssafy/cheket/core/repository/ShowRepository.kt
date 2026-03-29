package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.*
import kotlinx.coroutines.flow.Flow

data class ShowPage(
    val shows: List<Show>,
    val page: Int,
    val totalPages: Int,
    val totalElements: Int,
)

interface ShowRepository {
    suspend fun getShowsPage(
        regions: List<Int>? = null,
        sort: String? = null,
        keyword: String? = null,
        includeEnded: Boolean = false,
        page: Int = 0,
        size: Int = 20,
    ): ShowPage

    fun getShows(): Flow<List<Show>>
    fun getBannerSlides(): Flow<List<BannerSlide>>
    fun getCategories(): Flow<List<CategoryIcon>>
    fun getRankingItems(): Flow<List<RankingItem>>
    fun getOpenSchedule(): Flow<List<OpenScheduleItem>>
    fun getDiscountItems(): Flow<List<DiscountItem>>
    suspend fun getShowById(id: String): Show?
    suspend fun likeShow(showId: String)
    suspend fun unlikeShow(showId: String)
    suspend fun getLikedShows(): List<LikedShow>
}
