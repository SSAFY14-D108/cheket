package com.ssafy.cheket.core.repository.fake

import android.util.Log
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "FakeEventRepo"

class FakeEventRepository : EventRepository {
    override fun getEvents(): Flow<List<Event>> = flow {
        Log.d(TAG, "getEvents()")
        emit(MockDataSource.mockEvents)
    }

    override fun getBannerSlides(): Flow<List<BannerSlide>> = flow {
        Log.d(TAG, "getBannerSlides()")
        emit(MockDataSource.bannerSlides)
    }

    override fun getCategories(): Flow<List<CategoryIcon>> = flow {
        Log.d(TAG, "getCategories()")
        emit(MockDataSource.categories)
    }

    override fun getRankingItems(): Flow<List<RankingItem>> = flow {
        Log.d(TAG, "getRankingItems()")
        emit(MockDataSource.rankingItems)
    }

    override fun getOpenSchedule(): Flow<List<OpenScheduleItem>> = flow {
        Log.d(TAG, "getOpenSchedule()")
        emit(MockDataSource.openSchedule)
    }

    override fun getDiscountItems(): Flow<List<DiscountItem>> = flow {
        Log.d(TAG, "getDiscountItems()")
        emit(MockDataSource.discountItems)
    }

    override suspend fun getEventById(id: String): Event? {
        Log.d(TAG, "getEventById() id=$id")
        return MockDataSource.mockEvents.find { it.id == id }
    }
}
