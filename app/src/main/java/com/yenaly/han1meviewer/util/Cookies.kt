package com.yenaly.han1meviewer.util

import com.yenaly.han1meviewer.cookieMap
import com.yenaly.han1meviewer.preferenceSp
import com.yenaly.han1meviewer.ui.fragment.settings.HomeSettingsFragment
import okhttp3.Cookie

@JvmInline
value class CookieString(val cookie: String)

/**
 * 單純爲了[cookieMap]而作的函數。
 *
 * 第一次運行該函數后，會將string轉化成[cookieMap]；
 * 第二次及多次運行會直接返回map中的結果，該函數的功能部分會失效。
 *
 * 主要用於[CookieJar][okhttp3.CookieJar]，最好不要用到其他地方。
 */
internal fun CookieString.toCookieList(domain: String): List<Cookie> {
    if (cookie.isEmpty()) {
        return preferencesCookieList(domain)
    }
    cookieMap?.let { map ->
        return map[domain] ?: preferencesCookieList(domain)
    }
    val cookieList = mutableListOf<Cookie>().also {
        it += preferencesCookieList(domain)
    }
    cookie.split(';').forEach { cookie ->
        val name = cookie.substringBefore('=').trim()
        val value = cookie.substringAfter('=').trim()
        cookieList += Cookie.Builder().domain(domain).name(name).value(value).build()
    }
    cookieMap = mutableMapOf<String, List<Cookie>>().also { map ->
        map[domain] = cookieList
    }
    return cookieList
}

/**
 * 每次退出登入後都會清除cookie，但是這樣可能會清除掉很多保存在cookie中的偏好，比如影片語言之類。
 *
 * 讓[preferencesCookieList]成爲 存在偏好設置 但不存在個人信息 的[emptyList]
 */
private fun preferencesCookieList(domain: String): List<Cookie> {
    val videoLanguage =
        preferenceSp.getString(HomeSettingsFragment.VIDEO_LANGUAGE, "zh-CHT") ?: "zh-CHT"
    val videoLanguageCookie = Cookie.Builder().domain(domain)
        .name("user_lang")
        .value(videoLanguage)
        .build()
    return listOf(videoLanguageCookie)
}