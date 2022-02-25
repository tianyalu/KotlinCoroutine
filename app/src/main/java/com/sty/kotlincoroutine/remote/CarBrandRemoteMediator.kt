package com.sty.kotlincoroutine.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.sty.kotlincoroutine.db.AppCarBrandDatabase
import com.sty.kotlincoroutine.db.CarBrandEntity

/**
 * Author: ShiTianyi
 * Time: 2022/2/21 0021 19:23
 * Description:
 */
@ExperimentalPagingApi
class CarBrandRemoteMediator(
    private val api: CarBrandService,
    private val database: AppCarBrandDatabase
):
    RemoteMediator<Int, CarBrandEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CarBrandEntity>
    ): MediatorResult {
        //第一步，判断loadType

        //第二步，请求网络分页数据

        //第三步，插入数据库

    }
}