package com.sty.kotlincoroutine.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sty.kotlincoroutine.api.User
import com.sty.kotlincoroutine.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Author: ShiTianyi
 * Time: 2022/1/11 0011 20:55
 * Description:
 */
class MainViewModel : ViewModel(){
    val userLiveData = MutableLiveData<User>()

    private val userRepository = UserRepository()

    fun getUser(name: String) {
        viewModelScope.launch {
            userLiveData.value = userRepository.getUser(name)
        }
    }
}