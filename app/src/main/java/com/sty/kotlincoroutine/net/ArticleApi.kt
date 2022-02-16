package com.sty.kotlincoroutine.net

import com.sty.kotlincoroutine.model.Article
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 14:25
 * Description:
 */
interface ArticleApi {

    @GET("article")
    suspend fun searchArticles(
        @Query("key") key: String
    ): List<Article>
}