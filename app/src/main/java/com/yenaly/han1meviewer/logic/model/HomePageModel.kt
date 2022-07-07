package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 22:45
 */
data class HomePageModel(
    val avatarUrl: String?,
    val username: String?,
    val latestHanime: List<HanimeInfoModel>,
    val latestUpload: List<HanimeInfoModel>,
    val hotHanimeMonthly: List<HanimeInfoModel>,
    val hanimeCurrent: List<HanimeInfoModel>,
    val hanimeTheyWatched: List<HanimeInfoModel>
    // 首页TAG不想弄
)