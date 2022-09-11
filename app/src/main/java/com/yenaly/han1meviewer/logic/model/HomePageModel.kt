package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:45
 */
data class HomePageModel(
    val avatarUrl: String?,
    val username: String?,
    val latestHanime: MutableList<HanimeInfoModel>,
    val latestUpload: MutableList<HanimeInfoModel>,
    val hotHanimeMonthly: MutableList<HanimeInfoModel>,
    val hanimeCurrent: MutableList<HanimeInfoModel>,
    val hanimeTheyWatched: MutableList<HanimeInfoModel>
    // 首页TAG不想弄
)