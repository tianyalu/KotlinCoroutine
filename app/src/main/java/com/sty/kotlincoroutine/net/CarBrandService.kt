package com.sty.kotlincoroutine.net

import com.sty.kotlincoroutine.model.CarBrandItemModel
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:27
 * Description:
 */
interface CarBrandService {

    @GET("carBrand.do")
    suspend fun fetchCarBrandList(
        @Query("since") since: Int,
        @Query("page_size") pageSize: Int
    ): List<CarBrandItemModel>
}