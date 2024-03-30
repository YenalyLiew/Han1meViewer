package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:45
 */
data class HomePage(
    val avatarUrl: String?,
    val username: String?,
    val banner: Banner?,
    val latestHanime: MutableList<HanimeInfo>,
    val latestRelease: MutableList<HanimeInfo>,
    val latestUpload: MutableList<HanimeInfo>,
    val chineseSubtitle: MutableList<HanimeInfo>,
    val hanimeTheyWatched: MutableList<HanimeInfo>,
    val hanimeCurrent: MutableList<HanimeInfo>,
    val hotHanimeMonthly: MutableList<HanimeInfo>,
    // 首页TAG不想弄
) {
    data class Banner(
        val title: String,
        val description: String?,
        val picUrl: String,
        val videoCode: String,
    )
}