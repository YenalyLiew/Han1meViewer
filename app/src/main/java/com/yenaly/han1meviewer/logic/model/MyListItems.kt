package com.yenaly.han1meviewer.logic.model

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/05 005 15:30
 */
data class MyListItems<I>(
    val hanimeInfo: List<I>,
    /**
     * 清單的介紹，一般用於播放清單
     */
    var desc: String? = null,
    val csrfToken: String? = null,
)
