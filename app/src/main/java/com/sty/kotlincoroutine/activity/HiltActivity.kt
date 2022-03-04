package com.sty.kotlincoroutine.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.sty.kotlincoroutine.adapter.CarBrandAdapter
import com.sty.kotlincoroutine.adapter.FooterAdapter
import com.sty.kotlincoroutine.databinding.ActivityHiltBinding
import com.sty.kotlincoroutine.viewmodel.CarBrandViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HiltActivity : AppCompatActivity() {

    private val mBinding: ActivityHiltBinding by lazy {
        ActivityHiltBinding.inflate(layoutInflater)
    }

    private val mViewModel: CarBrandViewModel by viewModels()
    private val mCarBrandAdapter by lazy {
        CarBrandAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding.rcvList.adapter =
            mCarBrandAdapter.withLoadStateFooter(FooterAdapter(mCarBrandAdapter, this))
        mViewModel.data.observe(this, {
            mCarBrandAdapter.submitData(lifecycle, it)
            mBinding.swipeRefresh.isEnabled = false
        })

        lifecycleScope.launchWhenCreated {
            mCarBrandAdapter.loadStateFlow.collectLatest { state ->
                mBinding.swipeRefresh.isRefreshing = state.refresh is LoadState.Loading
            }
        }
    }
}