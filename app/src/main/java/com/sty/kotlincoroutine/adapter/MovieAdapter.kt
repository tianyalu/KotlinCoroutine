package com.sty.kotlincoroutine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sty.kotlincoroutine.databinding.ItemMovieBinding
import com.sty.kotlincoroutine.model.Movie

/**
 * Author: ShiTianyi
 * Time: 2022/2/16 0016 20:39
 * Description:
 */
class MovieAdapter(private val context: Context): PagingDataAdapter<Movie, BindingViewHolder>(object: DiffUtil.ItemCallback<Movie>(){
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        // Java 中 == 比较的是引用，equals比较的是内容
        // kotlin中 == 比较的是内容而不是引用
        return oldItem == newItem
    }

}) {
    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val movie = getItem(position)
        movie?.let {
            val binding = holder.binding as ItemMovieBinding
            binding.movie = it
            binding.networkImage = it.cover
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(context), parent, false)
        return BindingViewHolder(binding)
    }

}