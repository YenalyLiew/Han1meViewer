package com.yenaly.han1meviewer.logic.dao

import androidx.room.*
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/02 002 16:47
 */
@Dao
abstract class WatchHistoryDao {

    @Query("SELECT * FROM WatchHistoryEntity ORDER BY id DESC")
    abstract fun loadAllWatchHistories(): Flow<List<WatchHistoryEntity>>

    @Delete
    abstract suspend fun deleteWatchHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM WatchHistoryEntity")
    abstract suspend fun deleteAllWatchHistories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWatchHistory(history: WatchHistoryEntity)

    @Query("SELECT * FROM WatchHistoryEntity WHERE (`title` = :title) LIMIT 1")
    abstract suspend fun loadWatchHistory(title: String): WatchHistoryEntity?

    @Transaction
    open suspend fun insertOrUpdateSearchHistory(history: WatchHistoryEntity) {
        val dbEntity = loadWatchHistory(history.title)
        if (dbEntity != null) {
            deleteWatchHistory(dbEntity)
            insertWatchHistory(history)
            return
        }
        insertWatchHistory(history)
    }
}