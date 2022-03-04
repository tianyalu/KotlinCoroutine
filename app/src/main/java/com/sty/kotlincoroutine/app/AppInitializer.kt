package com.sty.kotlincoroutine.app

import android.content.Context
import androidx.startup.Initializer

/**
 * Author: ShiTianyi
 * Time: 2022/3/1 0001 20:00
 * Description:
 */
class AppInitializer: Initializer<Unit> {
    override fun create(context: Context) {
        AppHelper.init(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}