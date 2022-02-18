package com.sty.kotlincoroutine.model

import com.google.gson.annotations.SerializedName

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 20:11
 * Description:
 */
data class Movies(
    @SerializedName("subjects")
    val movieList: List<Movie>,
    @SerializedName("has_more")
    var hasMore: Boolean
)
