package com.yenaly.han1meviewer

import android.webkit.CookieManager
import androidx.core.app.NotificationManagerCompat
import com.yenaly.han1meviewer.util.CookieString
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.unsafeLazy

// android

internal val notificationManager by unsafeLazy { NotificationManagerCompat.from(applicationContext) }

// base

internal fun getHanimeVideoLink(videoCode: String) = HANIME_BASE_URL + "watch?v=" + videoCode

internal val videoUrlRegex = Regex("""hanime1\.me/watch\?v=(\d+)""")

internal fun String.toVideoCode() = videoUrlRegex.find(this)?.groupValues?.get(1)

// log in and log out

internal fun logout() {
    isAlreadyLogin = false
    loginCookie = CookieString(EMPTY_STRING)
    cookieMap = null
    CookieManager.getInstance().removeAllCookies(null)
}

internal fun login(cookie: CookieString) {
    isAlreadyLogin = true
    loginCookie = cookie
}