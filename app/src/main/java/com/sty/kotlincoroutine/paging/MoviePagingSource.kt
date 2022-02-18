package com.sty.kotlincoroutine.paging

import android.util.Log
import androidx.paging.PagingSource
import com.sty.kotlincoroutine.model.Movie
import com.sty.kotlincoroutine.net.MovieApi
import com.sty.kotlincoroutine.net.RetrofitClient

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 20:30
 * Description:
 */
class MoviePagingSource : PagingSource<Int, Movie>() {

    /**
     * currentPage, pageSize
     * 1, 8
     * 2, 8
     * 3, 8
     *
     * prevKey, nextKey
     * null, 2
     * 1, 3
     * 2, 4
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        try {
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            val movies = RetrofitClient.createApi(MovieApi::class.java)
                .getMovies(page = currentPage, pageSize = pageSize)
            Log.d("sty", "currentPage:$currentPage, pageSize:$pageSize")
            movies.hasMore = true  //因为服务器端受限，不会返回这个字段

            var prevKey: Int? = null
            var nextKey: Int? = null
            //prevKey = if(currentPage == 1) null else currentPage - 1
            //nextKey = if(movies.hasMore) currentPage + 1 else null

            val realPageSize = 8
            val initialLoadSize = 16
            if (currentPage == 1) {
                prevKey = null
                nextKey = initialLoadSize / realPageSize + 1
            } else {
                prevKey = currentPage - 1
                nextKey = if (movies.hasMore) currentPage + 1 else null
            }
            Log.d("sty", "prevKey:$prevKey, nextKey:$nextKey")

            return LoadResult.Page(
                data = movies.movieList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        }catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

}