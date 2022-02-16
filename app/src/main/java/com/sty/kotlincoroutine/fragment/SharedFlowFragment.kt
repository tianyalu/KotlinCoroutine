package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.FragmentSharedFlowBinding
import com.sty.kotlincoroutine.viewmodel.SharedFlowViewModel

/**
 * A simple [Fragment] subclass.
 * Use the [SharedFlowFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SharedFlowFragment : Fragment() {
    private val viewModel by viewModels<SharedFlowViewModel>()

    private val mBinding : FragmentSharedFlowBinding by lazy {
        FragmentSharedFlowBinding.inflate(layoutInflater)
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
            btnStart.setOnClickListener {
                viewModel.startRefresh()
            }
            btnStop.setOnClickListener {
                viewModel.stopRefresh()
            }
        }
    }

}