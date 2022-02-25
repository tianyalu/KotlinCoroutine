package com.sty.kotlincoroutine.mapper

/**
 * Author: ShiTianyi
 * Time: 2022/2/21 0021 21:01
 * Description:
 */
interface Mapper<I, O> {

    fun map(input: I): O
}