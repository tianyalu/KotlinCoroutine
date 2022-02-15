package com.sty.kotlincoroutine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sty.kotlincoroutine.databinding.ItemUserBinding
import com.sty.kotlincoroutine.db.User

/**
 * Author: ShiTianyi
 * Time: 2022/2/15 0015 20:19
 * Description:
 */
class UserAdapter(private val context: Context) : RecyclerView.Adapter<BindingViewHolder>() {
    private val data = ArrayList<User>()

    fun setData(data: List<User>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val item = data[position]
        val binding = holder.binding as ItemUserBinding
        binding.atvText.text = "${item.uid}, ${item.firstName}, ${item.age}"
    }

    override fun getItemCount() = data.size
}