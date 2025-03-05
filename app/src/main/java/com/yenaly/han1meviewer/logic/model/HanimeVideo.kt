package com.yenaly.han1meviewer.logic.model

import com.yenaly.han1meviewer.ResolutionLinkMap
import com.yenaly.yenaly_libs.utils.mapToArray
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/11 011 20:30
 */
@Serializable
data class HanimeVideo(
    val title: String,
    val coverUrl: String,
    val chineseTitle: String?,
    val introduction: String?,
    val uploadTime: LocalDate?,
    @Transient val views: String? = null,

    // resolution to video url
    val videoUrls: ResolutionLinkMap,

    val tags: List<String>,
    /**
     * 注意，這裏的myList是指用戶的播放清單playlist
     */
    @Transient val myList: MyList? = null,
    /**
     * 注意，這裏的playlist是指該影片的系列影片，並非用戶的播放清單
     */
    @Transient val playlist: Playlist? = null,
    @Transient val relatedHanimes: List<HanimeInfo> = emptyList(),
    val artist: Artist? = null,

    @Transient val favTimes: Int? = null,
    @Transient val isFav: Boolean = false,
    @Transient val csrfToken: String? = null,
    @Transient val currentUserId: String? = null,
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

        val titleArray get() = myListInfo.mapToArray(MyListInfo::title)
        val isSelectedArray get() = myListInfo.map(MyListInfo::isSelected).toBooleanArray()
    }

    data class Playlist(
        val playlistName: String?,
        val video: List<HanimeInfo>,
    )

    @Serializable
    data class Artist(
        val name: String,
        val avatarUrl: String,
        val genre: String,
        @Transient val post: POST? = null,
    ) {
        val isSubscribed: Boolean get() = post != null && post.isSubscribed

        data class POST(
            val userId: String,
            val artistId: String,
            val isSubscribed: Boolean,
        )
    }
}