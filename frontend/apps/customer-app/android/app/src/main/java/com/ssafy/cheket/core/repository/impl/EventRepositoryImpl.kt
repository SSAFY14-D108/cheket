package com.ssafy.cheket.core.repository.impl

import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EventRepositoryImpl : EventRepository {
    override fun getEvents(): Flow<List<Event>> = flow { emit(MockDataSource.mockEvents) }
    override fun getBannerSlides(): Flow<List<BannerSlide>> = flow { emit(MockDataSource.bannerSlides) }
    override fun getCategories(): Flow<List<CategoryIcon>> = flow { emit(MockDataSource.categories) }
    override fun getRankingItems(): Flow<List<RankingItem>> = flow { emit(MockDataSource.rankingItems) }
    override fun getOpenSchedule(): Flow<List<OpenScheduleItem>> = flow { emit(MockDataSource.openSchedule) }
    override fun getDiscountItems(): Flow<List<DiscountItem>> = flow { emit(MockDataSource.discountItems) }
    override suspend fun getEventById(id: String): Event? = MockDataSource.mockEvents.find { it.id == id }
}
