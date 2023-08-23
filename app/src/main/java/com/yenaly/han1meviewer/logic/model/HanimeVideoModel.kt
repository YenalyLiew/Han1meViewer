package com.yenaly.han1meviewer.logic.model

import com.yenaly.han1meviewer.ResolutionLinkMap
import java.util.Date

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/11 011 20:30
 */
data class HanimeVideoModel(
    val title: String,
    val coverUrl: String,
    val introduction: String?,
    val uploadTime: Date?,
    val views: String?,

    // resolution to video url
    val videoUrls: ResolutionLinkMap,

    val tags: List<String>,
    val playList: PlayList?,
    val relatedHanimes: List<HanimeInfoModel>,
    val artist: Artist?,

    var favTimes: Int?,
    var isFav: Boolean = false,
    val csrfToken: String? = null,
    val currentUserId: String? = null,
) {

    fun incrementFavTime() {
        if (favTimes != null) favTimes = favTimes!! + 1
        isFav = true
    }

    fun decrementFavTime() {
        if (favTimes != null) favTimes = favTimes!! - 1
        isFav = false
    }

    data class PlayList(
        val playListName: String?,
        val video: List<HanimeInfoModel>,
    )

    data class Artist(
        val name: String,
        val avatarUrl: String,
        val genre: String,
    )
}
