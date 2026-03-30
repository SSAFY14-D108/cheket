package com.ssafy.cheket.core.repository.fake

import android.util.Log
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.model.ResaleItem
import com.ssafy.cheket.core.repository.ResaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val TAG = "FakeResaleRepo"

class FakeResaleRepository : ResaleRepository {
    override fun getResaleItems(): Flow<List<ResaleItem>> = flow {
        Log.d(TAG, "getResaleItems()")
        emit(MockDataSource.mockResaleItems)
    }

    override fun getResaleGrouped(): Flow<List<ResaleGroupItem>> = flow {
        Log.d(TAG, "getResaleGrouped()")
        emit(MockDataSource.getResaleGrouped())
    }
}
