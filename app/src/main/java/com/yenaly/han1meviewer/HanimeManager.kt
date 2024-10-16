package com.yenaly.han1meviewer

import android.graphics.Color
import android.graphics.Typeface
import android.webkit.CookieManager
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.Preferences.isAlreadyLogin
import com.yenaly.han1meviewer.Preferences.loginCookie
import com.yenaly.han1meviewer.logic.network.HCookieJar
import com.yenaly.han1meviewer.util.CookieString
import kotlinx.serialization.json.Json

@JvmField
val HJson = Json {
    ignoreUnknownKeys = true
}

/**
 * 给用户显示的错误信息
 *
 * ぴえん化
 */
val Throwable.pienization: CharSequence get() = "🥺\n$localizedMessage"

// base

val hanimeSpannable
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
fun getHanimeVideoLink(videoCode: String) = HANIME_BASE_URL + "watch?v=" + videoCode

/**
 * 獲取 Hanime 影片**官方**下載地址
 */
fun getHanimeVideoDownloadLink(videoCode: String) =
    HANIME_BASE_URL + "download?v=" + videoCode

val videoUrlRegex = Regex("""hanime1\.(?:com|me)/watch\?v=(\d+)""")

fun String.toVideoCode() = videoUrlRegex.find(this)?.groupValues?.get(1)

// log in and log out

fun logout() {
    isAlreadyLogin = false
    loginCookie = CookieString(EMPTY_STRING)
    HCookieJar.cookieMap.clear()
    CookieManager.getInstance().removeAllCookies(null)
}

fun login(cookies: String) {
    isAlreadyLogin = true
    loginCookie = CookieString(cookies)
}

fun login(cookies: List<String>) {
    login(cookies.joinToString(";") {
        it.substringBefore(';')
    })
}