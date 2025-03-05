package com.yenaly.han1meviewer.logic.entity.download

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["videoId", "categoryId"],
    indices = [Index(value = ["categoryId"])],
)
data class HanimeCategoryCrossRef(
    val videoId: Int,
    val categoryId: Int,
)