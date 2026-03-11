package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.*
import kotlinx.coroutines.flow.Flow

interface ShowRepository {
    fun getShows(): Flow<List<Show>>
    fun getBannerSlides(): Flow<List<BannerSlide>>
    fun getCategories(): Flow<List<CategoryIcon>>
    fun getRankingItems(): Flow<List<RankingItem>>
    fun getOpenSchedule(): Flow<List<OpenScheduleItem>>
    fun getDiscountItems(): Flow<List<DiscountItem>>
    suspend fun getShowById(id: String): Show?
}
