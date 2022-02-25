package com.sty.kotlincoroutine.remote

import androidx.paging.*
import com.sty.kotlincoroutine.db.AppCarBrandDatabase
import com.sty.kotlincoroutine.db.AppDatabase
import com.sty.kotlincoroutine.db.CarBrandEntity
import com.sty.kotlincoroutine.mapper.Mapper
import com.sty.kotlincoroutine.model.CarBrandItemModel
import com.sty.kotlincoroutine.repository.CarBrandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Author: ShiTianyi
 * Time: 2022/2/21 0021 20:51
 * Description:
 */
class CarBrandRepositoryImpl(
    private val api: CarBrandService,
    private val database: AppCarBrandDatabase,
    private val mapper2ItemModel: Mapper<CarBrandEntity, CarBrandItemModel>
): CarBrandRepository {
    @ExperimentalPagingApi
    override fun fetchCarBrandList(): Flow<PagingData<CarBrandItemModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 8,
                prefetchDistance = 1,
                initialLoadSize = 16
            ),
            remoteMediator = CarBrandRemoteMediator(api, database) //请求网络数据，放入数据库
        ) {
            database.carBrandDao().getCarBrand() //从数据库拿到数据
        }.flow
            .flowOn(Dispatchers.IO)
            .map { pagingData ->
                pagingData.map { mapper2ItemModel.map(it) } //对数据进行转换，给到UI显示
            }
    }
}