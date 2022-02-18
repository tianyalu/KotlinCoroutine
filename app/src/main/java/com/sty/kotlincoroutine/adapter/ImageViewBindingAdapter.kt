package com.sty.kotlincoroutine.adapter

import android.graphics.Color
import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import com.sty.kotlincoroutine.R

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 21:05
 * Description:
 */
class ImageViewBindingAdapter {

    companion object {
        @JvmStatic
        @BindingAdapter("image")
        fun setImage(imageView: ImageView, url: String) {
            if(!TextUtils.isEmpty(url)) {
                Picasso.get().load(url).placeholder(R.drawable.ic_launcher_background).into(imageView)
            }else {
                imageView.setBackgroundColor(Color.GRAY)
            }
        }
    }

}