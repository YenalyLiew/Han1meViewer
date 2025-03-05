package com.yenaly.han1meviewer.logic.entity.download

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class VideoWithCategories(
    @Embedded
    val video: HanimeDownloadEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = HanimeCategoryCrossRef::class,
            parentColumn = "videoId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<DownloadCategoryEntity>,
)
