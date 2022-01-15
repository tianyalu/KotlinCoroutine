package com.sty.kotlincoroutine.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.ActivityMvvmBinding
import com.sty.kotlincoroutine.viewmodel.MainViewModel

class MvvmActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMvvmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_mvvm)
        binding.vm = mainViewModel
        binding.lifecycleOwner = this

        initView()
    }

    private fun initView() {
        binding.btnSubmitByAsyncTask.setOnClickListener {
            mainViewModel.getUser("xxx")
        }
    }
}