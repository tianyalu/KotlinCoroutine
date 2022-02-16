package com.sty.kotlincoroutine.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 17:04
 * Description:
 */
class NumberViewModel : ViewModel() {
    val number = MutableStateFlow(0)

    fun increment() {
        number.value++
    }

    fun decrement() {
        number.value--
    }
}