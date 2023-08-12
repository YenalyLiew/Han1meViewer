package com.yenaly.han1meviewer.logic.dao

import androidx.room.*
import com.yenaly.han1meviewer.logic.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 17:53
 */
@Dao
abstract class SearchHistoryDao {

    @Query("SELECT * FROM SearchHistoryEntity ORDER BY id DESC")
    abstract fun loadAllSearchHistories(): Flow<MutableList<SearchHistoryEntity>>

    @Query("SELECT * FROM SearchHistoryEntity WHERE `query` LIKE '%' || :keyword || '%' ORDER BY id DESC")
    abstract fun loadAllSearchHistories(keyword: String): Flow<MutableList<SearchHistoryEntity>>

    @Delete
    abstract suspend fun deleteSearchHistory(history: SearchHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSearchHistory(history: SearchHistoryEntity)

    @Query("SELECT * FROM SearchHistoryEntity WHERE (`query` = :query) LIMIT 1")
    abstract suspend fun loadSearchHistory(query: String): SearchHistoryEntity?

    @Query("DELETE FROM SearchHistoryEntity WHERE (`query` = :query)")
    abstract suspend fun deleteSearchHistoryByKeyword(query: String)

    @Transaction
    open suspend fun insertOrUpdateSearchHistory(entity: SearchHistoryEntity) {
        val dbEntity = loadSearchHistory(entity.query)
        if (dbEntity != null) {
            deleteSearchHistory(dbEntity)
            insertSearchHistory(entity)
            return
        }
        insertSearchHistory(entity)
    }
}