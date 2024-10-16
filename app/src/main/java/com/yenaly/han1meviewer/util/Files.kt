package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.yenaly.han1meviewer.FILE_PROVIDER_AUTHORITY
import com.yenaly.han1meviewer.HJson
import com.yenaly.yenaly_libs.utils.applicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

val hanimeVideoLocalFolder get() = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

const val DEF_VIDEO_TYPE = "mp4"

fun createDownloadName(title: String, quality: String, suffix: String = DEF_VIDEO_TYPE) =
    "${title}_${quality}.${suffix}"

fun getDownloadedHanimeFile(title: String, quality: String, suffix: String = DEF_VIDEO_TYPE): File {
    return File(hanimeVideoLocalFolder, createDownloadName(title, quality, suffix))
}

@Deprecated("不用了")
fun checkDownloadedHanimeFile(startsWith: String): Boolean {
    return hanimeVideoLocalFolder?.let { folder ->
        folder.listFiles()?.any { it.name.startsWith(startsWith) }
    } == true
}

/**
 * Must be Activity Context!
 */
fun Context.openDownloadedHanimeVideoLocally(
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
suspend fun InputStream.copyTo(
    out: OutputStream,
    contentLength: Long,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    progress: (suspend (Int) -> Unit)? = null,
): Long {
    return withContext(Dispatchers.IO) {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        var percent = 0
        while (bytes >= 0) {
            yield()
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            val newPercent = (bytesCopied * 100 / contentLength).toInt()
            if (newPercent != percent) {
                percent = newPercent
                progress?.invoke(percent.coerceAtMost(100))
            }
            bytes = read(buffer)
        }
        bytesCopied
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> loadAssetAs(filePath: String): T? {
    return try {
        applicationContext.assets.open(filePath).use { inputStream ->
            HJson.decodeFromStream<T>(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}