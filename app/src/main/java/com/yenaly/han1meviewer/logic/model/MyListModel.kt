package com.yenaly.han1meviewer.logic.model

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/05 005 15:30
 */
data class MyListModel(
    val hanimeInfo: List<HanimeInfoModel>,
    val csrfToken: String? = null
)
