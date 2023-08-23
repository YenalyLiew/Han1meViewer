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

    @Query("SELECT * FROM HanimeDownloadEntity WHERE downloadedLength == length ORDER BY id DESC")
    abstract fun loadAllDownloadedHanime(): Flow<MutableList<HanimeDownloadEntity>>

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