package com.sty.kotlincoroutine.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:44
 * Description:
 */
@Entity
data class CarBrandEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val page: Int = 0, //页码
    val icon: String
)
