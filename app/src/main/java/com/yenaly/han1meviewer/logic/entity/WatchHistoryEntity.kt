package com.yenaly.han1meviewer.logic.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/02 002 13:13
 */
@Entity
data class WatchHistoryEntity(
    val coverUrl: String,
    val title: String,
    val releaseDate: Long,
    val watchDate: Long,

    val videoCode: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
) {

    val releaseDateDays: Int
        get() = (releaseDate / (24 * 60 * 60 * 1000)).toInt()
}
