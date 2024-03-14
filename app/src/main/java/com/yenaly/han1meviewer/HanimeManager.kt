package com.yenaly.han1meviewer

import android.graphics.Color
import android.graphics.Typeface
import android.webkit.CookieManager
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.Preferences.loginCookie
import com.yenaly.han1meviewer.logic.network.HCookieJar
import com.yenaly.han1meviewer.util.CookieString

// base

internal val hanimeSpannable
    get() = null.spannable {
        "H".span {
            style(Typeface.BOLD)
            color(Color.RED)
        }
        "an1me".span {
            style(Typeface.BOLD)
        }
        "Viewer".text()
    }

/**
 * 獲取 Hanime 影片地址
 */
internal fun getHanimeVideoLink(videoCode: String) = HANIME_BASE_URL + "watch?v=" + videoCode

/**
 * 獲取 Hanime 影片**官方**下載地址
 */
internal fun getHanimeVideoDownloadLink(videoCode: String) =
    HANIME_BASE_URL + "download?v=" + videoCode

internal val videoUrlRegex = when (HANIME_BASE_URL) {
    HANIME_MAIN_BASE_URL -> Regex("""hanime1\.me/watch\?v=(\d+)""")
    HANIME_ALTER_BASE_URL -> Regex("""hanime1\.(?:com|me)/watch\?v=(\d+)""")
    else -> throw IllegalStateException("This URL has not been handled.")
}

internal fun String.toVideoCode() = videoUrlRegex.find(this)?.groupValues?.get(1)

// log in and log out

internal fun logout() {
    isAlreadyLogin = false
    loginCookie = CookieString(EMPTY_STRING)
    HCookieJar.cookieMap.clear()
    CookieManager.getInstance().removeAllCookies(null)
}

internal fun login(cookies: String) {
    isAlreadyLogin = true
    loginCookie = CookieString(cookies)
}

internal fun login(cookies: List<String>) {
    login(cookies.joinToString(";") {
        it.substringBefore(';')
    })
}