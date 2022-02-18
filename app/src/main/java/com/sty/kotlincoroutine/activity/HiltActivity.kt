package com.sty.kotlincoroutine.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sty.kotlincoroutine.databinding.ActivityHiltBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltActivity : AppCompatActivity() {

    private val mBinding: ActivityHiltBinding by lazy {
        ActivityHiltBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }
}