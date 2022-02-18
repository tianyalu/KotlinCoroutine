package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.adapter.MovieAdapter
import com.sty.kotlincoroutine.adapter.MovieLoadMoreAdapter
import com.sty.kotlincoroutine.databinding.FragmentPagingBinding
import com.sty.kotlincoroutine.viewmodel.MovieViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * A simple [Fragment] subclass.
 * Use the [PagingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PagingFragment : Fragment() {
    private val movieViewModel by viewModels<MovieViewModel>()

    private val mBinding : FragmentPagingBinding by lazy{
        FragmentPagingBinding.inflate(layoutInflater)
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

        context?.let {
            val movieAdapter = MovieAdapter(it)

            mBinding.apply {
                rcvList.adapter = movieAdapter.withLoadStateFooter(MovieLoadMoreAdapter(requireContext()))
                srlRefresh.setOnRefreshListener {
                    movieAdapter.refresh()
                }
            }

            lifecycleScope.launchWhenCreated {
                movieViewModel.loadMovie().collectLatest { pagingData ->
                    movieAdapter.submitData(pagingData)
                }
            }

            lifecycleScope.launchWhenCreated {
                movieAdapter.loadStateFlow.collectLatest { state ->
                    mBinding.srlRefresh.isRefreshing = state.refresh is LoadState.Loading
                }
            }
        }


    }

}