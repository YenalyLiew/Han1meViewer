package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.yenaly.yenaly_libs.utils.applicationContext
import java.io.File

internal val hanimeVideoLocalFolder get() = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

internal fun createDownloadName(title: String, quality: String) = "${title}_${quality}.mp4"

internal fun getDownloadedHanimeFile(title: String, quality: String): File {
    return File(hanimeVideoLocalFolder, createDownloadName(title, quality))
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
        this, "com.yenaly.han1meviewer.fileProvider", videoFile
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(fileUri, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}