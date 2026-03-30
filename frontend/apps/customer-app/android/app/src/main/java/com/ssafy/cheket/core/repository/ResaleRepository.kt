package com.ssafy.cheket.core.repository

import com.ssafy.cheket.core.model.ResaleGroupItem
import com.ssafy.cheket.core.model.ResaleItem
import kotlinx.coroutines.flow.Flow

interface ResaleRepository {
    fun getResaleItems(): Flow<List<ResaleItem>>
    fun getResaleGrouped(): Flow<List<ResaleGroupItem>>
}
