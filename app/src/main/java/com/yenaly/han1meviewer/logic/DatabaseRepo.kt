package com.yenaly.han1meviewer.logic

import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.logic.dao.DownloadDatabase
import com.yenaly.han1meviewer.logic.dao.HistoryDatabase
import com.yenaly.han1meviewer.logic.dao.MiscellanyDatabase
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.logic.entity.HKeyframeHeader
import com.yenaly.han1meviewer.logic.entity.HKeyframeType
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.yenaly_libs.utils.applicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 23:00
 */
object DatabaseRepo {

    object HKeyframe {
        private val hKeyframeDao = MiscellanyDatabase.instance.hKeyframeDao

        fun loadAll(keyword: String? = null) =
            if (keyword != null) hKeyframeDao.loadAll(keyword)
            else hKeyframeDao.loadAll()

        // #issue-106: 剧集分类
        @OptIn(ExperimentalSerializationApi::class)
        fun loadAllShared(): Flow<List<HKeyframeType>> = flow {
            val res = applicationContext.assets.let { assets ->
                assets.list("h_keyframes")?.asSequence()
                    ?.filter { it.endsWith(".json") }
                    ?.mapNotNull { fileName ->
                        try {
                            assets.open("h_keyframes/$fileName").use { inputStream ->
                                Json.decodeFromStream<HKeyframeEntity>(inputStream)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    ?.sortedWith(
                        compareBy<HKeyframeEntity> { it.group }.thenBy { it.episode }
                    )
                    ?.groupBy { it.group ?: "???" }
                    ?.flatMap { (group, entities) ->
                        listOf(HKeyframeHeader(title = group, attached = entities)) + entities
                    }
                    .orEmpty()
            }
            emit(res)
        }

        suspend fun findBy(videoCode: String) =
            hKeyframeDao.findBy(videoCode)

        @OptIn(ExperimentalSerializationApi::class)
        fun observe(videoCode: String): Flow<HKeyframeEntity?> {
            if (Preferences.sharedHKeyframesEnable) {
                return flow t@{
                    val find = hKeyframeDao.findBy(videoCode)
                    if (find == null || Preferences.sharedHKeyframesUseFirst) {
                        applicationContext.assets
                            .open("h_keyframes/$videoCode.json")
                            .use { inputStream ->
                                val entity = Json.decodeFromStream<HKeyframeEntity>(inputStream)
                                this@t.emit(entity)
                            }
                    } else {
                        hKeyframeDao.observe(videoCode).collect {
                            this@t.emit(it)
                        }
                    }
                }.catch t@{ e ->
                    e.printStackTrace()
                    hKeyframeDao.observe(videoCode).collect {
                        this@t.emit(it)
                    }
                }
            }
            return hKeyframeDao.observe(videoCode)
        }

        suspend fun insert(entity: HKeyframeEntity) = hKeyframeDao.insert(entity)

        suspend fun update(entity: HKeyframeEntity) = hKeyframeDao.update(entity)

        suspend fun delete(entity: HKeyframeEntity) =
            hKeyframeDao.delete(entity)

        suspend fun modifyKeyframe(
            videoCode: String,
            oldKeyframe: HKeyframeEntity.Keyframe, keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.modifyKeyframe(videoCode, oldKeyframe, keyframe)

        suspend fun appendKeyframe(
            videoCode: String, title: String,
            keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.appendKeyframe(videoCode, title, keyframe)

        suspend fun removeKeyframe(
            videoCode: String,
            keyframe: HKeyframeEntity.Keyframe,
        ) = hKeyframeDao.removeKeyframe(videoCode, keyframe)
    }

    object SearchHistory {
        private val searchHistoryDao = HistoryDatabase.instance.searchHistory

        @JvmOverloads
        fun loadAll(keyword: String? = null) =
            if (keyword.isNullOrBlank()) searchHistoryDao.loadAll()
            else searchHistoryDao.loadAll(keyword)

        suspend fun delete(history: SearchHistoryEntity) =
            searchHistoryDao.delete(history)

        suspend fun insert(history: SearchHistoryEntity) =
            searchHistoryDao.insertOrUpdate(history)

        suspend fun deleteByKeyword(query: String) =
            searchHistoryDao.deleteByKeyword(query)
    }

    object WatchHistory {
        private val watchHistoryDao = HistoryDatabase.instance.watchHistory

        fun loadAll() =
            watchHistoryDao.loadAll()

        suspend fun delete(history: WatchHistoryEntity) =
            watchHistoryDao.delete(history)

        suspend fun deleteAll() =
            watchHistoryDao.deleteAll()

        suspend fun insert(history: WatchHistoryEntity) =
            watchHistoryDao.insertOrUpdate(history)
    }

    object HanimeDownload {
        private val hanimeDownloadDao = DownloadDatabase.instance.hanimeDownloadDao

        fun loadAllDownloadingHanime() =
            hanimeDownloadDao.loadAllDownloadingHanime()

        fun loadAllDownloadedHanime(
            sortedBy: HanimeDownloadEntity.SortedBy,
            ascending: Boolean,
        ) = hanimeDownloadDao.loadAllDownloadedHanime(sortedBy, ascending)

        suspend fun deleteBy(videoCode: String, quality: String) =
            hanimeDownloadDao.deleteBy(videoCode, quality)

        suspend fun pauseAll() =
            hanimeDownloadDao.pauseAll()

        suspend fun delete(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.delete(entity)

        suspend fun insert(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.insert(entity)

        suspend fun update(entity: HanimeDownloadEntity) =
            hanimeDownloadDao.update(entity)

        suspend fun findBy(videoCode: String, quality: String) =
            hanimeDownloadDao.findBy(videoCode, quality)

        suspend fun isExist(videoCode: String, quality: String) =
            hanimeDownloadDao.isExist(videoCode, quality)
    }
}