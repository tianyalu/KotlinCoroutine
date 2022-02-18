package com.sty.kotlincoroutine.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:47
 * Description:
 */
@Dao
interface CarBrandDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarBrand(carBrandList: List<CarBrandEntity>)

    @Query("SELECT * FROM CarBrandEntity")
    fun getCarBrand(): PagingSource<Int, CarBrandEntity>

    @Query("DELETE FROM CarBrandEntity")
    suspend fun clearCarBrand()
}