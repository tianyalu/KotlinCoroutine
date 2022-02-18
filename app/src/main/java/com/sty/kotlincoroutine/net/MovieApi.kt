package com.sty.kotlincoroutine.net

import com.sty.kotlincoroutine.model.Movies
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 20:28
 * Description:
 */
interface MovieApi {

    // https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8&sort=recommend&page_limit=1000&page_start=0
    @GET("search_subjects")
    suspend fun getMovies(
        @Query("type") type: String = "movie",
        @Query("tag") tag: String = "热门",
        @Query("sort") sort: String = "recommend",
        @Query("page_start") page: Int,
        @Query("page_limit") pageSize: Int
    ): Movies
}