package com.sty.kotlincoroutine.adapter

import android.view.LayoutInflater
import android.view.View
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.sty.kotlincoroutine.databinding.ItemNetworkStateBinding

/**
 * Author: ShiTianyi
 * Time: 2022/3/1 0001 19:27
 * Description:
 */
class NetworkStateItemViewHolder(
    private val binding: ItemNetworkStateBinding,
    val retryCallback: () -> Unit
) : RecyclerView.ViewHolder(binding.root){

    fun bindData(data: LoadState) {
        binding.apply {
            //正在加载，显示进度条
            progress.isVisible = data is LoadState.Loading
            //加载失败，显示并点击重试按钮
            btnRetry.isVisible = data is LoadState.Error
            btnRetry.setOnClickListener { retryCallback }
            //加载失败显示错误原因
            tvMsg.isVisible = !(data as? LoadState.Error)?.error?.message.isNullOrBlank()
            tvMsg.text = (data as? LoadState.Error)?.error?.message
        }
    }
}

inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if(value) View.VISIBLE else View.GONE
    }