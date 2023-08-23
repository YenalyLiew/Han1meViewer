package com.yenaly.han1meviewer.logic.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 16:54
 */
@Deprecated("Use [HanimeDownloadEntity] instead.")
@Entity
data class HanimeDownloadedEntity(
    var coverUrl: String,
    var title: String,
    var releaseDate: Long,
    var addDate: Long,
    var videoCode: String,
    var videoUri: String,
    var quality: String,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
)