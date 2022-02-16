package com.sty.kotlincoroutine.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sty.kotlincoroutine.model.Article
import com.sty.kotlincoroutine.net.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 14:27
 * Description:
 */
class ArticleViewModel(app: Application) : AndroidViewModel(app) {

    val articles = MutableLiveData<List<Article>>()

    fun searchArticles(key: String) {
        viewModelScope.launch {
            flow {
                val list = RetrofitClient.articleApi.searchArticles(key)
                emit(list)
            }.flowOn(Dispatchers.IO)
                .catch { e -> e.printStackTrace() }
                .collect {
                    articles.value = it
                }
        }
    }
}