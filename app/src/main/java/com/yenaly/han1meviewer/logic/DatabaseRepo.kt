package com.yenaly.han1meviewer.logic

import com.yenaly.han1meviewer.logic.dao.DownloadDatabase
import com.yenaly.han1meviewer.logic.dao.HistoryDatabase
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 23:00
 */
object DatabaseRepo {

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