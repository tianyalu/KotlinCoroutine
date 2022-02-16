package com.sty.kotlincoroutine.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sty.kotlincoroutine.common.Event
import com.sty.kotlincoroutine.common.LocalEventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 17:29
 * Description:
 */
class SharedFlowViewModel: ViewModel() {
    private lateinit var job: Job

    fun startRefresh() {
        job = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                LocalEventBus.postEvent(Event(System.currentTimeMillis()))
            }
        }
    }

    fun stopRefresh() {
        job.cancel()
    }
}