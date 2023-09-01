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
    /**
     * 注意，這裏的myList是指用戶的播放清單playlist
     */
    val myList: MyList?,
    /**
     * 注意，這裏的playlist是指該影片的系列影片，並非用戶的播放清單
     */
    val playlist: Playlist?,
    val relatedHanimes: List<HanimeInfoModel>,
    val artist: Artist?,

    var favTimes: Int?,
    var isFav: Boolean = false,
    val csrfToken: String? = null,
    val currentUserId: String? = null,
) {

    fun incrementFavTime() {
        favTimes?.let { favTimes = it + 1 }
        isFav = true
    }

    fun decrementFavTime() {
        favTimes?.let { favTimes = it - 1 }
        isFav = false
    }

    data class MyList(
        var isWatchLater: Boolean,
        val myListInfo: List<MyListInfo>,
    ) {
        data class MyListInfo(
            val code: String,
            val title: String,
            var isSelected: Boolean,
        )

        val titleArray get() = myListInfo.map { it.title }.toTypedArray()
        val isSelectedArray get() = myListInfo.map { it.isSelected }.toBooleanArray()
    }

    data class Playlist(
        val playlistName: String?,
        val video: List<HanimeInfoModel>,
    )

    data class Artist(
        val name: String,
        val avatarUrl: String,
        val genre: String,
    )
}
