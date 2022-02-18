package com.sty.kotlincoroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sty.kotlincoroutine.model.Movie
import com.sty.kotlincoroutine.paging.MoviePagingSource
import kotlinx.coroutines.flow.Flow

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 20:33
 * Description:
 */
class MovieViewModel : ViewModel() {

    private val movies by lazy {
        Pager(
            config = PagingConfig(
                pageSize = 8,
                prefetchDistance = 1,
                initialLoadSize = 16
            ),
            pagingSourceFactory = {MoviePagingSource()}
        ).flow.cachedIn(viewModelScope)  //使viewModel的缓存生效
    }

    fun loadMovie() : Flow<PagingData<Movie>> = movies
}