package com.yenaly.han1meviewer

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.yenaly.han1meviewer.logic.network.HProxySelector
import com.yenaly.han1meviewer.ui.fragment.settings.HKeyframeSettingsFragment
import com.yenaly.han1meviewer.ui.fragment.settings.HomeSettingsFragment
import com.yenaly.han1meviewer.ui.fragment.settings.NetworkSettingsFragment
import com.yenaly.han1meviewer.ui.fragment.settings.PlayerSettingsFragment
import com.yenaly.han1meviewer.ui.view.video.HJzvdStd
import com.yenaly.han1meviewer.ui.view.video.HMediaKernel
import com.yenaly.han1meviewer.util.CookieString
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.getSpValue
import com.yenaly.yenaly_libs.utils.putSpValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

object Preferences {
    /**
     * [Preference][androidx.preference.PreferenceFragmentCompat]自帶的SP
     */
    val preferenceSp: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )

    // app 相關

    /**
     * 是否登入，一般跟[loginCookie]一起賦值
     */
    var isAlreadyLogin: Boolean
        get() = getSpValue(ALREADY_LOGIN, false)
        set(value) {
            loginStateFlow.value = value
            putSpValue(ALREADY_LOGIN, value)
        }

    val loginStateFlow = MutableStateFlow(isAlreadyLogin)

    /**
     * 保存的string格式的登入cookie
     */
    var loginCookie
        get() = CookieString(getSpValue(LOGIN_COOKIE, EMPTY_STRING))
        set(value) {
            loginCookieStateFlow.value = value
            putSpValue(LOGIN_COOKIE, value.cookie)
        }

    val loginCookieStateFlow = MutableStateFlow(loginCookie)

    // 更新 相關

    private const val UPDATE_NODE_ID = "update_node_id"

    var updateNodeId: String
        get() = getSpValue(UPDATE_NODE_ID, EMPTY_STRING)
        set(value) = putSpValue(UPDATE_NODE_ID, value)

    var lastUpdatePopupTime
        get() = getSpValue(HomeSettingsFragment.LAST_UPDATE_POPUP_TIME, 0L)
        set(value) = putSpValue(HomeSettingsFragment.LAST_UPDATE_POPUP_TIME, value)

    val updatePopupIntervalDays
        get() = preferenceSp.getInt(HomeSettingsFragment.UPDATE_POPUP_INTERVAL_DAYS, 0)

    val useCIUpdateChannel
        get() = preferenceSp.getBoolean(HomeSettingsFragment.USE_CI_UPDATE_CHANNEL, false)

    // Check if show update dialog.
    val isUpdateDialogVisible: Boolean
        get() {
            val now = Clock.System.now()
            val lastCheckTime = Instant.fromEpochSeconds(lastUpdatePopupTime)
            val interval = updatePopupIntervalDays
            return now > lastCheckTime + interval.days
        }

    // 設定 相關

    val switchPlayerKernel: String
        get() = preferenceSp.getString(
            PlayerSettingsFragment.SWITCH_PLAYER_KERNEL,
            HMediaKernel.Type.ExoPlayer.name
        ) ?: HMediaKernel.Type.ExoPlayer.name

    val showBottomProgress: Boolean
        get() = preferenceSp.getBoolean(
            PlayerSettingsFragment.SHOW_BOTTOM_PROGRESS,
            true
        )

    val playerSpeed: Float
        get() = preferenceSp.getString(
            PlayerSettingsFragment.PLAYER_SPEED,
            HJzvdStd.DEF_SPEED.toString()
        )?.toFloat() ?: HJzvdStd.DEF_SPEED

    val slideSensitivity: Int
        get() = preferenceSp.getInt(
            PlayerSettingsFragment.SLIDE_SENSITIVITY,
            HJzvdStd.DEF_PROGRESS_SLIDE_SENSITIVITY
        )

    val longPressSpeedTime: Float
        get() = preferenceSp.getString(
            PlayerSettingsFragment.LONG_PRESS_SPEED_TIMES,
            HJzvdStd.DEF_LONG_PRESS_SPEED_TIMES.toString()
        )?.toFloat() ?: HJzvdStd.DEF_LONG_PRESS_SPEED_TIMES

    val videoLanguage: String
        get() = preferenceSp.getString(HomeSettingsFragment.VIDEO_LANGUAGE, "zh-CHT") ?: "zh-CHT"

    val baseUrl: String
        get() = preferenceSp.getString(NetworkSettingsFragment.DOMAIN_NAME, HANIME_MAIN_BASE_URL)
            ?: HANIME_MAIN_BASE_URL

    val useBuiltInHosts: Boolean
        get() = preferenceSp.getBoolean(NetworkSettingsFragment.USE_BUILT_IN_HOSTS, false)

    // 關鍵H幀 相關

    val whenCountdownRemind: Int
        get() = preferenceSp.getInt(
            HKeyframeSettingsFragment.WHEN_COUNTDOWN_REMIND,
            HJzvdStd.DEF_COUNTDOWN_SEC
        ) * 1_000 // 越不了界，最大就30_000ms而已

    val showCommentWhenCountdown: Boolean
        get() = preferenceSp.getBoolean(
            HKeyframeSettingsFragment.SHOW_COMMENT_WHEN_COUNTDOWN,
            false
        )

    val hKeyframesEnable: Boolean
        get() = preferenceSp.getBoolean(
            HKeyframeSettingsFragment.H_KEYFRAMES_ENABLE,
            true
        )

    val sharedHKeyframesEnable: Boolean
        get() = preferenceSp.getBoolean(
            HKeyframeSettingsFragment.SHARED_H_KEYFRAMES_ENABLE,
            true
        )

    val sharedHKeyframesUseFirst: Boolean
        get() = preferenceSp.getBoolean(
            HKeyframeSettingsFragment.SHARED_H_KEYFRAMES_USE_FIRST,
            false
        )

    // 代理 相關

    val proxyType: Int
        get() = preferenceSp.getInt(
            NetworkSettingsFragment.PROXY_TYPE,
            HProxySelector.TYPE_SYSTEM
        )

    val proxyIp: String
        get() = preferenceSp.getString(NetworkSettingsFragment.PROXY_IP, EMPTY_STRING).orEmpty()

    val proxyPort: Int
        get() = preferenceSp.getInt(NetworkSettingsFragment.PROXY_PORT, -1)

    // 隐私 相關

    val isAnalyticsEnabled: Boolean
        get() = preferenceSp.getBoolean(HomeSettingsFragment.USE_ANALYTICS, true)

}