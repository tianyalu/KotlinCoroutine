package com.sty.kotlincoroutine.repository

import com.sty.kotlincoroutine.api.User
import com.sty.kotlincoroutine.api.userServiceApi

/**
 * Author: ShiTianyi
 * Time: 2022/1/11 0011 21:09
 * Description:
 */
class UserRepository {
    suspend fun getUser(name: String): User {
        return userServiceApi.getUser()
    }
}