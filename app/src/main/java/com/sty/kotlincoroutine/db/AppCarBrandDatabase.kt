package com.sty.kotlincoroutine.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 21:01
 * Description:
 */
//指定数据库中的表，版本号和是否在指定的schemaLocation目录下输出数据库架构信息
@Database(entities = [CarBrandEntity::class], version = 1, exportSchema = false)
abstract class AppCarBrandDatabase: RoomDatabase()  {
    //提供获取DAO类的抽象方法
    abstract fun carBrandDao(): CarBrandDao
}