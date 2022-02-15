package com.sty.kotlincoroutine.fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.sty.kotlincoroutine.R
import com.sty.kotlincoroutine.databinding.FragmentDownloadBinding
import com.sty.kotlincoroutine.download.DownloadManager
import com.sty.kotlincoroutine.download.DownloadStatus
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [DownloadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DownloadFragment : Fragment() {
    val URL = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fpic.jj20.com%2Fup%2Fallimg%2Fmn02%2F11291Z21250%2F19112Z21250-8.jpg&refer=http%3A%2F%2Fpic.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1647434280&t=b3331025082b80e552d850ddb44e1345"

    private val mBinding: FragmentDownloadBinding by lazy{
        FragmentDownloadBinding.inflate(layoutInflater)
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

        askForPermissions(Permission.WRITE_EXTERNAL_STORAGE) { _ ->
            lifecycleScope.launchWhenCreated {
                context?.apply {
                    //val file = File(getExternalFilesDir(null)?.path + "/sty/", "beauty.JPG")
                    // /storage/emulated/0/Android/data/com.sty.kotlincoroutine/files/sty/beauty.JPG
                    val file = File(
                        Environment.getExternalStorageDirectory()?.path + "/sty/",
                        "beauty.JPG"
                    )
                    // /storage/emulated/0/sty/beauty.JPG
                    DownloadManager.download(URL, file).collect { status ->
                        when (status) {
                            is DownloadStatus.Progress -> {
                                mBinding.apply {
                                    progressBar.progress = status.value
                                    tvProgress.text = "${status.value}%"
                                }
                            }
                            is DownloadStatus.Error -> {
                                Toast.makeText(context, "下载错误", Toast.LENGTH_SHORT).show()
                            }
                            is DownloadStatus.Done -> {
                                mBinding.apply {
                                    progressBar.progress = 100
                                    tvProgress.text = "100%"
                                }
                                Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show()

                            }
                            else -> {
                                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}