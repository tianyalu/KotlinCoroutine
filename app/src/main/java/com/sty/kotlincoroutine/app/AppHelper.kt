package com.sty.kotlincoroutine.app

import android.annotation.SuppressLint
import android.content.Context

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:21
 * Description:
 */


@SuppressLint("StaticFieldLeak")
object AppHelper {
    const val SERVER_URL = "http://10.16.0.196:8084/abc/"

    lateinit var mContext: Context

    fun init(context: Context) {
        this.mContext = context
    }
}