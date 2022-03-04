package com.sty.kotlincoroutine.binding

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load
import com.sty.kotlincoroutine.R

/**
 * Author: ShiTianyi
 * Time: 2022/2/25 0025 21:15
 * Description:
 */
@BindingAdapter("bindingAvatar")
fun bindingAvatar(imageView: ImageView, url:String) {
    Log.d("sty", "loading image: $url")
    imageView.load(url) {
        crossfade(true) //淡入淡出
        placeholder(R.mipmap.ic_launcher_round) //占位图
        error(R.mipmap.ic_launcher_round)
    }
}