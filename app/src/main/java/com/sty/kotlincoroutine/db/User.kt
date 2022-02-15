package com.sty.kotlincoroutine.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Author: ShiTianyi
 * Time: 2022/2/15 0015 18:23
 * Description:
 */
@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "age") val age: Int
)
