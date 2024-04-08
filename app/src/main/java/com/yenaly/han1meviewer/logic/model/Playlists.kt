package com.yenaly.han1meviewer.logic.model

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/08/26 026 17:47
 */
data class Playlists(
    val playlists: List<Playlist>,
    val csrfToken: String? = null,
) {
    data class Playlist(
        val listCode: String,
        var title: String,
        var total: Int,
    )
}

/**
 * 用於 修改播放清單 Flow 的返回值
 */
data class ModifiedPlaylistArgs(
    var title: String,
    var desc: String,
    var isDeleted: Boolean,
)
