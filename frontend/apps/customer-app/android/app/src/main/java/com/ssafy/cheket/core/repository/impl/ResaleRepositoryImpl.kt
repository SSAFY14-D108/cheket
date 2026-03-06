package com.ssafy.cheket.core.repository.impl

import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.model.ResaleItem
import com.ssafy.cheket.core.repository.ResaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ResaleRepositoryImpl : ResaleRepository {
    override fun getResaleItems(): Flow<List<ResaleItem>> = flow { emit(MockDataSource.mockResaleItems) }
    override fun getResaleGrouped(): Flow<List<ResaleGroupItem>> = flow { emit(MockDataSource.getResaleGrouped()) }
}
