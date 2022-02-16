package com.sty.kotlincoroutine.net

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 14:23
 * Description:
 */
object RetrofitClient {

    private val instance: Retrofit by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            //.client(builder.build())
            .baseUrl("http://10.16.1.51:8084/abc/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val builder = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request() //Current Request
            val response = chain.proceed(originalRequest) //Get response of the request

            val bodyString = response.body()?.string()
            Log.d("sty", "request url: ${originalRequest.url()}\n request header: ${originalRequest.headers()}")
            Log.d("sty", "response code: ${response.code()}\n response message: " +
                    "${response.message()}\n response header: ${response.headers()} \nresponse body: $bodyString")
            response.newBuilder().body(ResponseBody.create(response.body()?.contentType(), bodyString)).build()
        }

    val articleApi: ArticleApi by lazy {
        instance.create(ArticleApi::class.java)
    }
}