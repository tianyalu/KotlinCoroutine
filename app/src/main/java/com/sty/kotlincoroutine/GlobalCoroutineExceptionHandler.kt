package com.sty.kotlincoroutine

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Author: ShiTianyi
 * Time: 2022/1/19 0019 20:42
 * Description:
 */
class GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Log.e("sty", "UnHandled Coroutine Exception: $exception")
    }
}