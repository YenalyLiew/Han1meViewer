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
    abstract fun loadAll(): Flow<MutableList<SearchHistoryEntity>>

    @Query("SELECT * FROM SearchHistoryEntity WHERE `query` LIKE '%' || :keyword || '%' ORDER BY id DESC")
    abstract fun loadAll(keyword: String): Flow<MutableList<SearchHistoryEntity>>

    @Delete
    abstract suspend fun delete(history: SearchHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(history: SearchHistoryEntity)

    @Query("SELECT * FROM SearchHistoryEntity WHERE (`query` = :query) LIMIT 1")
    abstract suspend fun find(query: String): SearchHistoryEntity?

    @Query("DELETE FROM SearchHistoryEntity WHERE (`query` = :query)")
    abstract suspend fun deleteByKeyword(query: String)

    @Transaction
    open suspend fun insertOrUpdate(entity: SearchHistoryEntity) {
        val dbEntity = find(entity.query)
        if (dbEntity != null) {
            delete(dbEntity)
            insert(entity)
            return
        }
        insert(entity)
    }
}