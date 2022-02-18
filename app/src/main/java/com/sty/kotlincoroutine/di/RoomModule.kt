package com.sty.kotlincoroutine.di

import android.app.Application
import androidx.room.Room
import com.sty.kotlincoroutine.db.AppCarBrandDatabase
import com.sty.kotlincoroutine.db.AppDatabase
import com.sty.kotlincoroutine.db.CarBrandDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:56
 * Description:
 */
@InstallIn(ApplicationComponent::class)
@Module
class RoomModule {

    @Singleton
    @Provides
    fun provideAppDataBase(application: Application): AppCarBrandDatabase {
        return Room.databaseBuilder(application, AppCarBrandDatabase::class.java, "car_home.db")
            .build()
    }

    @Singleton
    @Provides
    fun provideCarBrandDao(database: AppCarBrandDatabase) : CarBrandDao {
        return database.carBrandDao()
    }
}