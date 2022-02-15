package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.FragmentDemoBinding


class DemoFragment : Fragment() {

    private val mBinding: FragmentDemoBinding by lazy {
        FragmentDemoBinding.inflate(layoutInflater)
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
        mBinding.btnFlowAndDownload.setOnClickListener {
            findNavController().navigate(R.id.action_demoFragment_to_downloadFragment)
        }
    }
}