package com.sty.kotlincoroutine.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private val mBinding: ActivityDemoBinding by lazy {
        ActivityDemoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }
}