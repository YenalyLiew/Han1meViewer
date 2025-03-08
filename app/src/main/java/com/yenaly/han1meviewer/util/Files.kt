package com.yenaly.han1meviewer.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.yenaly.han1meviewer.FILE_PROVIDER_AUTHORITY
import com.yenaly.han1meviewer.FROM_DOWNLOAD
import com.yenaly.han1meviewer.HJson
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.showShortToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Deprecated(
    "Use alternative",
    ReplaceWith(
        "HFileManager.appDownloadFolder",
        imports = ["com.yenaly.han1meviewer.HFileManager"]
    )
)
val hanimeVideoLocalFolder get() = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

@Deprecated(
    "Use alternative",
    ReplaceWith(
        "HFileManager.DEF_VIDEO_TYPE",
        imports = ["com.yenaly.han1meviewer.HFileManager"]
    )
)
const val DEF_VIDEO_TYPE = "mp4"

@Deprecated(
    "Use alternative",
    ReplaceWith(
        "HFileManager.createVideoName(title, quality, suffix)",
        imports = ["com.yenaly.han1meviewer.HFileManager"]
    )
)
fun createDownloadName(title: String, quality: String, suffix: String = DEF_VIDEO_TYPE) =
    "${title}_${quality}.${suffix}"

@Deprecated(
    "Use alternative",
    ReplaceWith(
        "HFileManager.getDownloadVideoFile(videoCode, title, quality, suffix)",
        imports = ["com.yenaly.han1meviewer.HFileManager"]
    )
)
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
    uri: String, onFileNotFound: (() -> Unit)? = null,
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
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        showShortToast(R.string.action_not_support)
        e.printStackTrace()
    }
}

fun Context.openDownloadedHanimeVideoInActivity(videoCode: String) {
    val intent = Intent(this, VideoActivity::class.java)
    intent.putExtra(FROM_DOWNLOAD, true)
    intent.putExtra(VIDEO_CODE, videoCode)
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
        this@copyTo.use {
            var bytesCopied: Long = 0
            val buffer = ByteArray(bufferSize)
            var bytes = read(buffer)
            var percent = 0
            while (bytes >= 0) {
                ensureActive()
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
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> loadAssetAs(filePath: String): T? = runCatching {
    applicationContext.assets.open(filePath).use { inputStream ->
        HJson.decodeFromStream<T>(inputStream)
    }
}.getOrNull()