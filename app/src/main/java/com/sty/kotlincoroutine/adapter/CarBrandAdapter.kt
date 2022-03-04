package com.sty.kotlincoroutine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.sty.kotlincoroutine.databinding.ItemCarbrandBinding
import com.sty.kotlincoroutine.model.CarBrandItemModel

/**
 * Author: ShiTianyi
 * Time: 2022/2/28 0028 19:06
 * Description:
 */
class CarBrandAdapter(private val context: Context) :
    PagingDataAdapter<CarBrandItemModel, BindingViewHolder>(object: DiffUtil.ItemCallback<CarBrandItemModel>() {
    override fun areItemsTheSame(oldItem: CarBrandItemModel, newItem: CarBrandItemModel): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: CarBrandItemModel,
        newItem: CarBrandItemModel
    ): Boolean {
        return oldItem == newItem
    }

}) {
    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        getItem(position).let { item ->
            val binding = holder.binding as ItemCarbrandBinding
            binding.carBrand = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = ItemCarbrandBinding.inflate(LayoutInflater.from(context), parent, false)
        return BindingViewHolder(binding)
    }
}