package com.sty.api

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Author: ShiTianyi
 * Time: 2022/2/11 0011 18:14
 * Description:
 */
data class User(val lastName: String, val age: Int)

val userServiceApi: UserServiceApi by lazy {
    val retrofit = retrofit2.Retrofit.Builder()
        .client(OkHttpClient.Builder().addInterceptor {
            it.proceed(it.request()).apply {
                Log.d("sty", "request:${code()}")
            }
        }.build())
        //.baseUrl("http://10.16.0.200:8084/abc/")
        .baseUrl("http://10.16.1.51:8084/abc/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    retrofit.create(UserServiceApi::class.java)
}

interface UserServiceApi {
    @GET("person")
    //fun loadUser(@Query("name") name: String) : Call<User>
    fun loadUser() : Call<User>

    @GET("person")
    suspend fun getUser() : User
}