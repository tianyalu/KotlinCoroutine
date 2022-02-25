package com.sty.kotlincoroutine.binding

import android.widget.ImageView
import coil.load
import com.sty.kotlincoroutine.R

/**
 * Author: ShiTianyi
 * Time: 2022/2/25 0025 21:15
 * Description:
 */
fun bindingAvatar(imageView: ImageView, url:String) {
    imageView.load(url) {
        crossfade(true) //淡入淡出
        placeholder(R.mipmap.ic_launcher_round) //占位图
    }
}