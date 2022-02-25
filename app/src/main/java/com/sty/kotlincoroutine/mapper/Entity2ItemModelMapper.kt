package com.sty.kotlincoroutine.mapper

import com.sty.kotlincoroutine.db.CarBrandEntity
import com.sty.kotlincoroutine.model.CarBrandItemModel

/**
 * Author: ShiTianyi
 * Time: 2022/2/21 0021 21:02
 * Description:
 */
class Entity2ItemModelMapper : Mapper<CarBrandEntity, CarBrandItemModel> {
    override fun map(input: CarBrandEntity): CarBrandItemModel =
        CarBrandItemModel(id = input.id, name = input.name, icon = input.icon)
}