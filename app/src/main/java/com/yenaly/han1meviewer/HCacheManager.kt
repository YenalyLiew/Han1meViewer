package com.yenaly.han1meviewer

import androidx.annotation.WorkerThread
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.model.HanimeVideo
import com.yenaly.yenaly_libs.utils.createFileIfNotExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @since 2025/3/5 20:11
 */
object HCacheManager {

    private const val CACHE_INFO_FILE = "info.json"

    /**
     * 保存 HanimeVideo 信息，用于下载后直接在 APP 内观看
     */
    @OptIn(ExperimentalSerializationApi::class)
    @WorkerThread
    fun saveHanimeVideoInfo(videoCode: String, info: HanimeVideo) {
        val folder = HFileManager.getDownloadVideoFolder(videoCode)
        val file = File(folder, CACHE_INFO_FILE)
        file.createFileIfNotExists()
        HJson.encodeToStream(info, file.outputStream())
    }

    /**
     * 加载 HanimeVideo 信息，用于下载后直接在 APP 内观看
     */
    @OptIn(ExperimentalSerializationApi::class)
    fun loadHanimeVideoInfo(videoCode: String): Flow<HanimeVideo?> {
        return flow {
            val entity = DatabaseRepo.HanimeDownload.find(videoCode)
            if (entity != null) {
                val folder = HFileManager.getDownloadVideoFolder(videoCode)
                val cacheFile = File(folder, CACHE_INFO_FILE)
                val info = kotlin.runCatching {
                    if (cacheFile.exists()) HJson.decodeFromStream<HanimeVideo?>(cacheFile.inputStream()) else null
                }.getOrNull()
                emit(
                    info?.copy(
                        videoUrls = linkedMapOf(
                            entity.quality to HanimeLink(
                                entity.videoUri, HFileManager.DEF_VIDEO_TYPE
                            )
                        ),
                        coverUrl = entity.coverUri ?: entity.coverUrl
                    )
                )
            } else {
                emit(null)
            }
        }.flowOn(Dispatchers.IO)
    }
}