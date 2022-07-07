package com.yenaly.han1meviewer.logic.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:56
 */
data class HanimeInfoModel(
    val title: String,
    val coverUrl: String,
    val redirectLink: String,
    val duration: String? = null,
    val uploader: String? = null,
    val views: String? = null,
    val uploadTime: String? = null,
    val genre: String? = null,

    val isPlaying: Boolean = false, // for video list only.

    override var itemType: Int
) : MultiItemEntity {
    companion object {
        const val NORMAL = 0
        const val SIMPLIFIED = 1
    }
}
