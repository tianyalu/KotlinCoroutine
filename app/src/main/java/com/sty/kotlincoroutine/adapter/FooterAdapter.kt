package com.sty.kotlincoroutine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sty.kotlincoroutine.databinding.ItemNetworkStateBinding

/**
 * Author: ShiTianyi
 * Time: 2022/3/1 0001 19:26
 * Description:
 */
class FooterAdapter(
    val adapter: CarBrandAdapter,
    val context: Context
) : LoadStateAdapter<NetworkStateItemViewHolder>() {
    override fun onBindViewHolder(holder: NetworkStateItemViewHolder, loadState: LoadState) {
        //水平居中
        val params = holder.itemView.layoutParams
        if(params is StaggeredGridLayoutManager.LayoutParams) {
            params.isFullSpan = true
        }
        holder.bindData(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateItemViewHolder {
        val binding = ItemNetworkStateBinding.inflate(LayoutInflater.from(context), parent, false)
        return NetworkStateItemViewHolder(binding) { adapter.retry() }
    }

}