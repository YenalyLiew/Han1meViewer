package com.yenaly.han1meviewer

import androidx.preference.PreferenceManager
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.getSpValue
import com.yenaly.yenaly_libs.utils.putSpValue
import okhttp3.Cookie

/**
 * [Preference][androidx.preference.PreferenceFragmentCompat]自帶的SP
 */
internal val preferenceSp get() = PreferenceManager.getDefaultSharedPreferences(applicationContext)

/**
 * 是否登入，一般跟[loginCookie]一起賦值
 */
inline var alreadyLogin
    get() = getSpValue(ALREADY_LOGIN, false)
    set(value) = putSpValue(ALREADY_LOGIN, value)

/**
 * 保存的string格式的登入cookie
 */
inline var loginCookie
    get() = CookieString(getSpValue(LOGIN_COOKIE, EMPTY_STRING))
    set(value) = putSpValue(LOGIN_COOKIE, value.cookie)

/**
 * cookie的map
 */
internal var cookieMap: MutableMap<String, List<Cookie>>? = null