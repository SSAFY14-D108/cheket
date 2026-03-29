package com.ssafy.cheket.core.repository.fake

import android.util.Log
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.*
import com.ssafy.cheket.core.repository.ShowPage
import com.ssafy.cheket.core.repository.ShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "FakeShowRepo"

class FakeShowRepository : ShowRepository {

    override suspend fun getShowsPage(
        regions: List<Int>?,
        sort: String?,
        keyword: String?,
        includeEnded: Boolean,
        page: Int,
        size: Int,
    ): ShowPage {
        val all = MockDataSource.mockShows
        val regionNames = regions?.map { RegionCode.toName(it) }
        val filtered = all.filter { show ->
            (regionNames == null || show.region in regionNames) &&
                    (keyword == null || show.name.contains(keyword, ignoreCase = true)) &&
                    (includeEnded || show.status != ShowStatus.COMPLETED)
        }
        val start = (page * size).coerceAtMost(filtered.size)
        val end = ((page + 1) * size).coerceAtMost(filtered.size)
        return ShowPage(
            shows = filtered.subList(start, end),
            page = page,
            totalPages = ((filtered.size + size - 1) / size).coerceAtLeast(1),
            totalElements = filtered.size,
        )
    }

    override suspend fun likeShow(showId: String) {
        Log.d(TAG, "likeShow() showId=$showId")
    }

    override suspend fun unlikeShow(showId: String) {
        Log.d(TAG, "unlikeShow() showId=$showId")
    }

    override fun getShows(): Flow<List<Show>> = flow {
        Log.d(TAG, "getShows()")
        emit(MockDataSource.mockShows)
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

    override suspend fun getShowById(id: String): Show? {
        Log.d(TAG, "getShowById() id=$id")
        return MockDataSource.mockShows.find { it.id == id }
    }

    override suspend fun getLikedShows(): List<LikedShow> {
        Log.d(TAG, "getLikedShows()")
        return emptyList()
    }
}
