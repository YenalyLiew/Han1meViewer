package com.yenaly.han1meviewer.logic

import com.yenaly.han1meviewer.logic.dao.HistoryDatabase
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 23:00
 */
object DatabaseRepo {
    private val searchHistoryDao =
        HistoryDatabase.getInstance(applicationContext).searchHistory

    fun loadAllSearchHistories() =
        searchHistoryDao.loadAllSearchHistories()

    suspend fun deleteSearchHistory(history: SearchHistoryEntity) =
        searchHistoryDao.deleteSearchHistory(history)

    suspend fun insertSearchHistory(history: SearchHistoryEntity) =
        searchHistoryDao.insertOrUpdateSearchHistory(history)

    private val watchHistoryDao =
        HistoryDatabase.getInstance(applicationContext).watchHistory

    fun loadAllWatchHistories() =
        watchHistoryDao.loadAllWatchHistories()

    suspend fun deleteWatchHistory(history: WatchHistoryEntity) =
        watchHistoryDao.deleteWatchHistory(history)

    suspend fun deleteAllWatchHistories() =
        watchHistoryDao.deleteAllWatchHistories()

    suspend fun insertWatchHistory(history: WatchHistoryEntity) =
        watchHistoryDao.insertOrUpdateSearchHistory(history)
}