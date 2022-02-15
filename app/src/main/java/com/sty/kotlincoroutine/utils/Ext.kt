package com.sty.kotlincoroutine.utils

import java.io.InputStream
import java.io.OutputStream

/**
 * Author: ShiTianyi
 * Time: 2022/2/14 0014 20:10
 * Description:
 */
inline fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, progress: (Long) -> Unit) : Long{
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
        progress(bytesCopied)
    }
    return bytesCopied
}