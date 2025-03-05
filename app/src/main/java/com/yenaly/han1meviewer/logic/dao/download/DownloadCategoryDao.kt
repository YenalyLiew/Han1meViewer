package com.yenaly.han1meviewer.logic.dao.download

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.yenaly.han1meviewer.logic.entity.download.DownloadCategoryEntity
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DownloadCategoryDao {
    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        "SELECT * FROM HanimeDownloadEntity " +
                "INNER JOIN HanimeCategoryCrossRef ON HanimeDownloadEntity.id = HanimeCategoryCrossRef.videoId " +
                "WHERE HanimeCategoryCrossRef.categoryId = :categoryId AND HanimeDownloadEntity.downloadedLength == HanimeDownloadEntity.length"
    )
    abstract fun getVideosForCategory(categoryId: Int): Flow<List<HanimeDownloadEntity>>

    @Query("SELECT * FROM DownloadCategoryEntity")
    abstract fun getAllCategories(): Flow<MutableList<DownloadCategoryEntity>>
}