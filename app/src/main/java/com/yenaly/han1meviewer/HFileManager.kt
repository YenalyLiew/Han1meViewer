package com.yenaly.han1meviewer

import android.os.Environment
import com.yenaly.yenaly_libs.utils.makeFolderNoMedia
import java.io.File

object HFileManager {

    const val HANIME_DOWNLOAD_FOLDER = "hanime_download"

    private val illegalCharsRegex = Regex("""["*/:<>?\\|\x00-\x1F\x7F]""")

    val appDownloadFolder: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            APP_NAME
        ).also { file -> file.makeFolderNoMedia() }

    const val DEF_VIDEO_TYPE = "mp4"
    const val DEF_VIDEO_COVER_TYPE = "png"

    fun createVideoName(
        title: String, quality: String, suffix: String = DEF_VIDEO_TYPE
    ) = "${title.replaceAllIllegalChars()}_${quality}.${suffix}"

    fun createVideoCoverName(
        title: String, suffix: String = DEF_VIDEO_COVER_TYPE
    ) = "${title.replaceAllIllegalChars()}.${suffix}"

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

    /**
     * Replace all illegal characters in the string with "_"
     *
     * 若文件名中有非法字符，则替换为"_"，以避免文件名错误
     */
    private fun String.replaceAllIllegalChars(): String {
        return illegalCharsRegex.replace(this, "_")
    }
}