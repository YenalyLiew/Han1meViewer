@file:JvmName("FileUtil")

package com.yenaly.yenaly_libs.utils

import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyTo(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    progress: (Long) -> Unit
): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        progress.invoke(bytesCopied)
        bytes = read(buffer)
    }
    return bytesCopied
}

val File?.folderSize: Long
    get() {
        var size = 0L
        val files = this?.listFiles()
        files?.forEach { file -> size += if (file.isDirectory) file.folderSize else file.length() }
        return size
    }