package com.yenaly.han1meviewer

object FirebaseConstants {
    // <editor-fold desc="Analytics">

    /**
     * 高级搜索使用统计
     */
    const val ADV_SEARCH_OPT = "advanced_search_options"

    /**
     * 关键H帧使用统计
     */
    const val H_KEYFRAMES = "h_keyframes"

    // </editor-fold>

    // <editor-fold desc="Crashlytics">

    /**
     * 当前是否为登录状态
     */
    const val LOGIN_STATE = "login_state"

    /**
     * 当前APP内使用语言
     */
    const val APP_LANGUAGE = "app_language"

    /**
     * 当前APP来源，debug、release或ci
     */
    const val VERSION_SOURCE = "version_source"

    /**
     * 当前正在下载的任务数量
     */
    const val RUNNING_DOWNLOAD_WORK_COUNT = "running_download_work_count"

    // </editor-fold>

    // <editor-fold desc="Remote Config">

    /**
     * 是否启用 CI 更新。
     *
     * 由于当前使用了 Firebase，所以希望正式版更新时，减少 CI 更新的概率。
     * 方便更好地控制更新以及崩溃数据统计。
     */
    const val ENABLE_CI_UPDATE = "enable_ci_update"

    val remoteConfigDefaults: Map<String, Any> = mapOf(
        ENABLE_CI_UPDATE to true
    )

    // </editor-fold>
}