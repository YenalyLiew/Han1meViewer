package com.yenaly.han1meviewer

import android.webkit.CookieManager
import androidx.core.app.NotificationManagerCompat
import com.yenaly.han1meviewer.util.CookieString
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.unsafeLazy

// base

internal val notificationManager by unsafeLazy { NotificationManagerCompat.from(applicationContext) }

internal fun getHanimeVideoLink(videoCode: String) = HANIME_BASE_URL + "watch?v=" + videoCode

// 務必保證v=後面都是數字！！一般在程式内特定環境使用，分析剪貼簿時不要用！！
internal fun String.toVideoCode() = substringAfter("watch?v=")

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