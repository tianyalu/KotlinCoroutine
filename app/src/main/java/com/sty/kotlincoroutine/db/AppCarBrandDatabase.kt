package com.sty.kotlincoroutine.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 21:01
 * Description:
 */
@Database(entities = [CarBrandEntity::class], version = 1, exportSchema = false)
abstract class AppCarBrandDatabase: RoomDatabase()  {
    abstract fun carBrandDao(): CarBrandDao
}