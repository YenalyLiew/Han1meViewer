package com.yenaly.han1meviewer.logic.model

import com.yenaly.han1meviewer.ResolutionLinkMap
import kotlinx.datetime.LocalDate

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/11 011 20:30
 */
data class HanimeVideo(
    val title: String,
    val coverUrl: String,
    val chineseTitle: String?,
    val introduction: String?,
    val uploadTime: LocalDate?,
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
    val relatedHanimes: List<HanimeInfo>,
    val artist: Artist?,

    val favTimes: Int?,
    val isFav: Boolean = false,
    val csrfToken: String? = null,
    val currentUserId: String? = null,
) {

    fun incFavTime() = copy(favTimes = favTimes?.let { it + 1 }, isFav = true)

    fun decFavTime() = copy(favTimes = favTimes?.let { it - 1 }, isFav = false)

    // 為保證兼容性，不能直接用天數
    val uploadTimeMillis: Long
        get() = uploadTime?.let {
            it.toEpochDays().toLong() * 24 * 60 * 60 * 1000
        } ?: 0L

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
        val video: List<HanimeInfo>,
    )

    data class Artist(
        val name: String,
        val avatarUrl: String,
        val genre: String,
    )
}