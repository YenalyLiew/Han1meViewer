package com.yenaly.han1meviewer.logic.entity.download

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadCategoryEntity(
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)