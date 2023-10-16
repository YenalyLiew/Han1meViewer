package com.yenaly.han1meviewer.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/18 018 23:07
 */
@Dao
abstract class HanimeDownloadDao {
    @Query("SELECT * FROM HanimeDownloadEntity WHERE downloadedLength <> length ORDER BY id DESC")
    abstract fun loadAllDownloadingHanime(): Flow<MutableList<HanimeDownloadEntity>>

    open fun loadAllDownloadedHanime(
        sortedBy: HanimeDownloadEntity.SortedBy,
        ascending: Boolean,
    ): Flow<MutableList<HanimeDownloadEntity>> {
        return when (sortedBy) {
            HanimeDownloadEntity.SortedBy.TITLE -> loadAllDownloadedHanimeByTitle(ascending)
            HanimeDownloadEntity.SortedBy.ID -> loadAllDownloadedHanimeById(ascending)
        }
    }

    @Query(
        "SELECT * FROM HanimeDownloadEntity WHERE downloadedLength == length ORDER BY " +
                "CASE WHEN :ascending THEN title END ASC, CASE WHEN NOT :ascending THEN title END DESC"
    )
    abstract fun loadAllDownloadedHanimeByTitle(ascending: Boolean): Flow<MutableList<HanimeDownloadEntity>>

    @Query(
        "SELECT * FROM HanimeDownloadEntity WHERE downloadedLength == length ORDER BY " +
                "CASE WHEN :ascending THEN id END ASC, CASE WHEN NOT :ascending THEN id END DESC"
    )
    abstract fun loadAllDownloadedHanimeById(ascending: Boolean): Flow<MutableList<HanimeDownloadEntity>>

    @Query("DELETE FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode AND `quality` = :quality)")
    abstract suspend fun deleteBy(videoCode: String, quality: String)

    @Query("UPDATE HanimeDownloadEntity SET `isDownloading` = 0")
    abstract suspend fun pauseAll()

    @Delete
    abstract suspend fun delete(entity: HanimeDownloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: HanimeDownloadEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(entity: HanimeDownloadEntity): Int

    @Query("SELECT * FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode AND `quality` = :quality) LIMIT 1")
    abstract suspend fun findBy(videoCode: String, quality: String): HanimeDownloadEntity?

    @Transaction
    open suspend fun isExist(videoCode: String, quality: String): Boolean {
        return findBy(videoCode, quality) != null
    }
}