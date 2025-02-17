@file:JvmName("FileUtil")

package com.yenaly.yenaly_libs.utils

import androidx.annotation.WorkerThread
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

@WorkerThread
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

/**
 * 创建文件夹并在文件夹内创建.nomedia文件
 *
 * 为了防止媒体库扫描到文件夹内的文件
 */
fun File.makeFolderNoMedia() {
    if (!exists()) {
        mkdirs()
    } else if (!isDirectory) {
        return
    }
    val noMedia = File(this, ".nomedia")
    if (!noMedia.exists()) {
        noMedia.createNewFile()
    }
}

fun File.createFileIfNotExists(): Boolean {
    return if (!exists()) {
        parentFile?.mkdirs()
        createNewFile()
    } else {
        isFile
    }
}

fun File.createDirIfNotExists(): Boolean {
    return if (!exists()) mkdirs() else isDirectory
}

@WorkerThread
fun File.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(readBytes())).toString(16).padStart(32, '0')
}