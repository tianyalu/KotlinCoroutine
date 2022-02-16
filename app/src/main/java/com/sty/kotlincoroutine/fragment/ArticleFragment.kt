package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.adapter.ArticleAdapter
import com.sty.kotlincoroutine.databinding.FragmentArticleBinding
import com.sty.kotlincoroutine.viewmodel.ArticleViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A simple [Fragment] subclass.
 * Use the [ArticleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArticleFragment : Fragment() {

    private val viewModel by viewModels<ArticleViewModel>()

    private val mBinding: FragmentArticleBinding by lazy {
        FragmentArticleBinding.inflate(layoutInflater)
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

        //获取关键字
        lifecycleScope.launchWhenCreated {
            mBinding.etSearch.textWatcherFlow().collect {
                Log.d("sty", "collect keywords: $it")
                viewModel.searchArticles(it)
            }
        }

        context?.let {
            val adapter = ArticleAdapter(it)
            mBinding.rcvList.adapter = adapter
            viewModel.articles.observe(viewLifecycleOwner, { articles ->
                adapter.setData(articles)
            })
        }
    }

    private fun TextView.textWatcherFlow(): Flow<String> = callbackFlow {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                this@callbackFlow.trySend(s.toString()).isSuccess
            }
        }

        addTextChangedListener(textWatcher)
        awaitClose { removeTextChangedListener(textWatcher) }
    }
}