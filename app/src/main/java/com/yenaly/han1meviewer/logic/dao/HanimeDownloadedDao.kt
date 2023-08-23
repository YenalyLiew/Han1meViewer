@file:Suppress("DEPRECATION")

package com.yenaly.han1meviewer.logic.dao

import androidx.room.*
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadedEntity
import kotlinx.coroutines.flow.Flow

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/07 007 18:24
 */
@Deprecated("Use [HanimeDownloadDao] instead.")
@Dao
abstract class HanimeDownloadedDao {
    @Query("SELECT * FROM HanimeDownloadedEntity ORDER BY id DESC")
    abstract fun loadAllDownloadedHanime(): Flow<MutableList<HanimeDownloadedEntity>>

    @Query("DELETE FROM HanimeDownloadedEntity WHERE (`videoCode` = :videoCode)")
    abstract suspend fun deleteDownloadedHanimeByVideoCode(videoCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDownloadedHanime(entity: HanimeDownloadedEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateDownloadedHanime(entity: HanimeDownloadedEntity)

    @Query("SELECT * FROM HanimeDownloadedEntity WHERE (`videoCode` = :videoCode) LIMIT 1")
    abstract suspend fun loadDownloadedHanimeByVideoCode(videoCode: String): HanimeDownloadedEntity?

    @Transaction
    open suspend fun updateDownloadedHanimeQualityByVideoCode(videoCode: String, quality: String) {
        val entity = loadDownloadedHanimeByVideoCode(videoCode)
        if (entity != null) {
            entity.quality = quality
            updateDownloadedHanime(entity)
        }
    }
}