package com.yenaly.han1meviewer

import android.os.Environment
import com.yenaly.yenaly_libs.utils.makeFolderNoMedia
import java.io.File

@Suppress("NOTHING_TO_INLINE")
object HFileManager {

    const val HANIME_DOWNLOAD_FOLDER = "hanime_download"

    val appDownloadFolder: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APP_NAME
        ).also { file -> file.makeFolderNoMedia() }

    const val DEF_VIDEO_TYPE = "mp4"
    const val DEF_VIDEO_COVER_TYPE = "png"

    inline fun createVideoName(
        title: String, quality: String, suffix: String = DEF_VIDEO_TYPE
    ) = "${title}_${quality}.${suffix}"

    inline fun createVideoCoverName(
        title: String, suffix: String = DEF_VIDEO_COVER_TYPE
    ) = "${title}.${suffix}"

    fun getDownloadVideoFolder(videoCode: String): File {
        return File(appDownloadFolder, "$HANIME_DOWNLOAD_FOLDER/$videoCode")
    }

    fun getDownloadVideoFile(
        videoCode: String, title: String, quality: String,
        suffix: String = DEF_VIDEO_TYPE
    ): File {
        return File(getDownloadVideoFolder(videoCode), createVideoName(title, quality, suffix))
    }

    fun getDownloadVideoCoverFile(
        videoCode: String, title: String,
        suffix: String = DEF_VIDEO_COVER_TYPE
    ): File {
        return File(getDownloadVideoFolder(videoCode), createVideoCoverName(title, suffix))
    }
}