package com.yenaly.han1meviewer.logic

import com.yenaly.han1meviewer.logic.dao.DownloadDatabase
import com.yenaly.han1meviewer.logic.dao.HistoryDatabase
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadedEntity
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 23:00
 */
object DatabaseRepo {
    private val searchHistoryDao = HistoryDatabase.instance.searchHistory

    @JvmOverloads
    fun loadAllSearchHistories(keyword: String? = null) =
        if (keyword.isNullOrBlank()) searchHistoryDao.loadAllSearchHistories()
        else searchHistoryDao.loadAllSearchHistories(keyword)

    suspend fun deleteSearchHistory(history: SearchHistoryEntity) =
        searchHistoryDao.deleteSearchHistory(history)

    suspend fun insertSearchHistory(history: SearchHistoryEntity) =
        searchHistoryDao.insertOrUpdateSearchHistory(history)

    suspend fun deleteSearchHistoryByKeyword(query: String) =
        searchHistoryDao.deleteSearchHistoryByKeyword(query)

    private val watchHistoryDao = HistoryDatabase.instance.watchHistory

    fun loadAllWatchHistories() =
        watchHistoryDao.loadAllWatchHistories()

    suspend fun deleteWatchHistory(history: WatchHistoryEntity) =
        watchHistoryDao.deleteWatchHistory(history)

    suspend fun deleteAllWatchHistories() =
        watchHistoryDao.deleteAllWatchHistories()

    suspend fun insertWatchHistory(history: WatchHistoryEntity) =
        watchHistoryDao.insertOrUpdateSearchHistory(history)

    private val hanimeDownloadedDao = DownloadDatabase.instance.hanimeDownloadedDao

    fun loadAllDownloadedHanime() =
        hanimeDownloadedDao.loadAllDownloadedHanime()

    suspend fun deleteDownloadedHanimeByVideoCode(videoCode: String) =
        hanimeDownloadedDao.deleteDownloadedHanimeByVideoCode(videoCode)

    suspend fun insertDownloadedHanime(entity: HanimeDownloadedEntity) =
        hanimeDownloadedDao.insertDownloadedHanime(entity)

    suspend fun loadDownloadedHanimeByVideoCode(videoCode: String) =
        hanimeDownloadedDao.loadDownloadedHanimeByVideoCode(videoCode)

    suspend fun updateDownloadedHanime(entity: HanimeDownloadedEntity) =
        hanimeDownloadedDao.updateDownloadedHanime(entity)
}