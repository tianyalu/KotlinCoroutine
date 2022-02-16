package com.sty.kotlincoroutine.common

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 17:30
 * Description:
 */
object LocalEventBus {
    val events = MutableSharedFlow<Event>()

    suspend fun postEvent(event: Event) {
        events.emit(event)
    }
}

data class Event(val timestamp: Long)