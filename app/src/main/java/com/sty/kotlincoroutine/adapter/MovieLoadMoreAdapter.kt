package com.sty.kotlincoroutine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.sty.kotlincoroutine.databinding.LayoutLoadingMoreBinding

/**
 * Author: ShiTianyi
 * Time: 2022/2/17 0017 21:08
 * Description:
 */
class MovieLoadMoreAdapter(private val context: Context) : LoadStateAdapter<BindingViewHolder>() {
    override fun onBindViewHolder(holder: BindingViewHolder, loadState: LoadState) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BindingViewHolder {
        val binding = LayoutLoadingMoreBinding.inflate(LayoutInflater.from(context), parent, false)
        return BindingViewHolder(binding)
    }
}