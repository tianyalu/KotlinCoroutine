package com.sty.kotlincoroutine.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.sty.kotlincoroutine.model.CarBrandItemModel
import com.sty.kotlincoroutine.repository.CarBrandRepository

/**
 * Author: ShiTianyi
 * Time: 2022/2/25 0025 19:47
 * Description:
 */
class CarBrandViewModel @ViewModelInject constructor(
    private val carBrandRepository: CarBrandRepository
) : ViewModel() {
    val data: LiveData<PagingData<CarBrandItemModel>> =
        carBrandRepository
            .fetchCarBrandList()
            .cachedIn(viewModelScope) //使viewModel的缓存生效
            .asLiveData()
}
