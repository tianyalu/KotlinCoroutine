package com.sty.kotlincoroutine.download

import java.io.File

/**
 * Author: ShiTianyi
 * Time: 2022/2/14 0014 20:00
 * Description:
 */
sealed class DownloadStatus {
    object None : DownloadStatus()
    data class Progress(val value: Int) : DownloadStatus()
    data class Error(val throwable: Throwable) : DownloadStatus()
    data class Done(val file: File) : DownloadStatus()
}
