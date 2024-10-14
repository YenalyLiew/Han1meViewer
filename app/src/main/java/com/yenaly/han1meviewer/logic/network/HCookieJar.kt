package com.yenaly.han1meviewer.logic.network

import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.util.toLoginCookieList
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * 用於管理 Cookie。
 *
 * #issue-71: 我竟然栽倒在 Cookie 管理上好幾年了！你去看我以前的管理方式，
 * 是完全錯誤的，竟然還能維持應用正常運行，太離譜了！怪不得切換簡體繁體一直不起作用！
 *
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/13 013 15:20
 */
class HCookieJar : CookieJar {

    companion object {
        @JvmStatic
        val cookieMap: MutableMap<String, MutableList<Cookie>> = mutableMapOf()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieMap[url.host]
            ?: Preferences.loginCookieStateFlow.value.toLoginCookieList(url.host)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieMap[url.host] = cookies.toMutableList().also {
            it += Preferences.loginCookieStateFlow.value.toLoginCookieList(url.host)
        }
    }
}