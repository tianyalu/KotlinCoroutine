package com.sty.kotlincoroutine.repository

import androidx.paging.PagingData
import com.sty.kotlincoroutine.model.CarBrandItemModel
import kotlinx.coroutines.flow.Flow

/**
 * Author: ShiTianyi
 * Time: 2022/2/21 0021 20:48
 * Description:
 */
interface CarBrandRepository {

    fun fetchCarBrandList(): Flow<PagingData<CarBrandItemModel>>
}