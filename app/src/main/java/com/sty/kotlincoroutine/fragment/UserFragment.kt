package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.adapter.UserAdapter
import com.sty.kotlincoroutine.databinding.FragmentUserBinding
import com.sty.kotlincoroutine.viewmodel.UserViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    private val viewModel by viewModels<UserViewModel>()

    private val mBinding: FragmentUserBinding by lazy {
        FragmentUserBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBinding.apply {
            btnAdd.setOnClickListener {
                viewModel.insert(
                    etId.text.toString().toInt(),
                    etName.text.toString(),
                    etAge.text.toString().toInt()
                )
            }
        }

        context?.let {
            val adapter = UserAdapter(it)
            mBinding.rvList.adapter = adapter
            mBinding.rvList.layoutManager = LinearLayoutManager(it)
            lifecycleScope.launchWhenCreated {
                viewModel.getAll().collect { value ->
                    adapter.setData(value)
                }
            }
        }
    }
}