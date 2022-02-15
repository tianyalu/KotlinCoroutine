package com.sty.kotlincoroutine.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sty.kotlincoroutine.db.AppDatabase
import com.sty.kotlincoroutine.db.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Author: ShiTianyi
 * Time: 2022/2/15 0015 19:33
 * Description:
 */
class UserViewModel(app: Application): AndroidViewModel(app) {
    fun insert(uid: Int, name: String, age: Int) {
        viewModelScope.launch {
            val user = User(uid, name, age)
            AppDatabase.getInstance(getApplication())
                .userDao()
                .insert(user)
            Log.d("sty", "insert user: $user")
        }
    }

    fun getAll(): Flow<List<User>> {
        return AppDatabase.getInstance(getApplication())
            .userDao()
            .getAll()
            .catch { e -> e.printStackTrace() }
            .flowOn(Dispatchers.IO)
    }
}