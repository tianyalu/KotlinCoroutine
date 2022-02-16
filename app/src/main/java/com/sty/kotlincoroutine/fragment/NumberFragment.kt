package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.FragmentNumberBinding
import com.sty.kotlincoroutine.viewmodel.NumberViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [NumberFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NumberFragment : Fragment() {
    private val viewModel by viewModels<NumberViewModel>()
    private val mBinding: FragmentNumberBinding by lazy {
        FragmentNumberBinding.inflate(layoutInflater)
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
            btnPlus.setOnClickListener {
                viewModel.increment()
            }

            btnMinus.setOnClickListener {
                viewModel.decrement()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.number.collect { value ->
                mBinding.tvNumber.text = "$value"
            }
        }
    }
}