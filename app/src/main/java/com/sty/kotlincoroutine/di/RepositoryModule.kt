package com.sty.kotlincoroutine.di

import com.sty.kotlincoroutine.db.AppCarBrandDatabase
import com.sty.kotlincoroutine.mapper.Entity2ItemModelMapper
import com.sty.kotlincoroutine.remote.CarBrandRepositoryImpl
import com.sty.kotlincoroutine.remote.CarBrandService
import com.sty.kotlincoroutine.repository.CarBrandRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * Author: ShiTianyi
 * Time: 2022/2/18 0018 20:56
 * Description:
 */
@InstallIn(ActivityComponent::class)
@Module
class RepositoryModule {

    @ActivityScoped
    @Provides
    fun provideCarBrandRepository(
        api: CarBrandService,
        database: AppCarBrandDatabase
    ): CarBrandRepository {
        return CarBrandRepositoryImpl(api, database, Entity2ItemModelMapper())
    }

}