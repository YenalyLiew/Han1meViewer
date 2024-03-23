package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.yenaly.han1meviewer.FILE_PROVIDER_AUTHORITY
import com.yenaly.yenaly_libs.utils.applicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

internal val hanimeVideoLocalFolder get() = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

internal fun createDownloadName(title: String, quality: String, suffix: String = "mp4") =
    "${title}_${quality}.${suffix}"

internal fun getDownloadedHanimeFile(title: String, quality: String, suffix: String = "mp4"): File {
    return File(hanimeVideoLocalFolder, createDownloadName(title, quality, suffix))
}

@Deprecated("不用了")
internal fun checkDownloadedHanimeFile(startsWith: String): Boolean {
    return hanimeVideoLocalFolder?.let { folder ->
        folder.listFiles()?.any { it.name.startsWith(startsWith) }
    } ?: false
}

/**
 * Must be Activity Context!
 */
internal fun Context.openDownloadedHanimeVideoLocally(
    uri: String,
    onFileNotFound: (() -> Unit)? = null,
) {
    val videoFile = uri.toUri().toFile()
    if (!videoFile.exists()) {
        onFileNotFound?.invoke()
        return
    }
    val fileUri = FileProvider.getUriForFile(
        this, FILE_PROVIDER_AUTHORITY, videoFile
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(fileUri, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}

/**
 * copyTo with progress
 */
fun InputStream.copyTo(
    out: OutputStream,
    contentLength: Long,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    progress: ((Int) -> Unit)? = null,
): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    var percent = 0
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        val newPercent = (bytesCopied * 100 / contentLength).toInt()
        if (newPercent != percent) {
            percent = newPercent
            progress?.invoke(percent)
        }
        bytes = read(buffer)
    }
    return bytesCopied
}