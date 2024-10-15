package com.yenaly.han1meviewer

object FirebaseConstants {
    // analytics
    const val ADV_SEARCH_OPT = "advanced_search_options"
    const val H_KEYFRAMES = "h_keyframes"

    // crashlytics
    const val LOGIN_STATE = "login_state"
    const val APP_LANGUAGE = "app_language"
    const val VERSION_SOURCE = "version_source"

    // remote config

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
}