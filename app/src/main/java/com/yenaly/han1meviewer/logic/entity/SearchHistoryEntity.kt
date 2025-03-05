package com.yenaly.han1meviewer.logic.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/22 022 18:16
 */
@Entity
data class SearchHistoryEntity(
    val query: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
