package com.yenaly.han1meviewer.logic.dao.download

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.entity.download.VideoWithCategories
import com.yenaly.han1meviewer.logic.state.DownloadState
import kotlinx.coroutines.flow.Flow

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/18 018 23:07
 */
@Dao
abstract class HanimeDownloadDao {
    /**
     * 获取所有未下载完成的任务
     *
     * 名字起得不好
     */
    @Query("SELECT * FROM HanimeDownloadEntity WHERE state != ${DownloadState.Mask.FINISHED} ORDER BY id DESC")
    abstract fun loadAllDownloadingHanime(): Flow<MutableList<HanimeDownloadEntity>>

    /**
     * 获取所有正在下载的任务，单次
     */
    //@Query("SELECT * FROM HanimeDownloadEntity WHERE isDownloading = 1 ORDER BY id DESC")
    @Query("SELECT * FROM HanimeDownloadEntity WHERE state != ${DownloadState.Mask.FINISHED} ORDER BY id DESC")
    abstract suspend fun loadAllDownloadingHanimeOnce(): MutableList<HanimeDownloadEntity>

    /**
     * 获取部分正在下载的任务，单次
     */
    //@Query("SELECT * FROM HanimeDownloadEntity WHERE isDownloading = 1 ORDER BY id DESC LIMIT :limit")
    @Query("SELECT * FROM HanimeDownloadEntity WHERE state != ${DownloadState.Mask.FINISHED} ORDER BY id DESC LIMIT :limit")
    abstract suspend fun loadDownloadingHanimeOnce(limit: Int): MutableList<HanimeDownloadEntity>

    @Query(
        "SELECT * FROM HanimeDownloadEntity WHERE state = ${DownloadState.Mask.FINISHED} ORDER BY " +
                "CASE WHEN :ascending THEN title END ASC, CASE WHEN NOT :ascending THEN title END DESC"
    )
    @Transaction
    abstract fun loadAllDownloadedHanimeByTitle(ascending: Boolean): Flow<MutableList<VideoWithCategories>>

    @Query(
        "SELECT * FROM HanimeDownloadEntity WHERE state = ${DownloadState.Mask.FINISHED} ORDER BY " +
                "CASE WHEN :ascending THEN id END ASC, CASE WHEN NOT :ascending THEN id END DESC"
    )
    @Transaction
    abstract fun loadAllDownloadedHanimeById(ascending: Boolean): Flow<MutableList<VideoWithCategories>>

    @Query("DELETE FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode AND `quality` = :quality)")
    @Deprecated("查屁")
    abstract suspend fun delete(videoCode: String, quality: String)

    @Query("DELETE FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode)")
    abstract suspend fun delete(videoCode: String)

    //@Query("UPDATE HanimeDownloadEntity SET `isDownloading` = 0")
    @Query("UPDATE HanimeDownloadEntity SET `state` = ${DownloadState.Mask.PAUSED}")
    abstract suspend fun pauseAll()

    @Delete
    abstract suspend fun delete(entity: HanimeDownloadEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    abstract suspend fun insert(entity: HanimeDownloadEntity)

    @Update(onConflict = OnConflictStrategy.Companion.REPLACE)
    abstract suspend fun update(entity: HanimeDownloadEntity): Int

    @Query("SELECT * FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode AND `quality` = :quality) LIMIT 1")
    abstract suspend fun find(videoCode: String, quality: String): HanimeDownloadEntity?

    @Query("SELECT * FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode) LIMIT 1")
    abstract suspend fun find(videoCode: String): HanimeDownloadEntity?

    @Query("SELECT COUNT(*) FROM HanimeDownloadEntity WHERE (`videoCode` = :videoCode)")
    @Deprecated("查屁")
    abstract suspend fun countBy(videoCode: String): Int
}