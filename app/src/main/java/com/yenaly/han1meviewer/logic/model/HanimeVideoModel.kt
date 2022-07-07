package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/11 011 20:30
 */
data class HanimeVideoModel(
    val title: String,
    val coverUrl: String,
    val introduction: String?,
    val uploadTimeWithViews: String,

    // resolution to video url
    val videoUrls: LinkedHashMap<String, String>,

    val tags: List<String>,
    val playList: PlayList?,
    val relatedHanimes: List<HanimeInfoModel>,

    val csrfToken: String? = null,
    val currentUserId: String? = null
) {
    data class PlayList(
        val playListName: String,
        val video: List<HanimeInfoModel>
    )
}
