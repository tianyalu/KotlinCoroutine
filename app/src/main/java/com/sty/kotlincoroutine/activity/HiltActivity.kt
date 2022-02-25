package com.sty.kotlincoroutine.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sty.kotlincoroutine.databinding.ActivityHiltBinding
import com.sty.kotlincoroutine.viewmodel.CarBrandViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltActivity : AppCompatActivity() {

    private val mBinding: ActivityHiltBinding by lazy {
        ActivityHiltBinding.inflate(layoutInflater)
    }

    private val mViewModel: CarBrandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mViewModel.data.observe(this, {

        })
    }
}