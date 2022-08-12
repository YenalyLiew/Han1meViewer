package com.yenaly.han1meviewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.webkit.CookieManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.yenaly_libs.utils.*
import okhttp3.Cookie
import java.io.File

// Utils Only For This App!!! //

/**
 * dynamically create tag chips.
 */
internal fun ChipGroup.createTags(tags: List<String>) {
    for (tag in tags) {
        val chip = LayoutInflater.from(context)
            .inflate(R.layout.item_video_tag_chip, this, false) as Chip
        chip.text = tag
        chip.setOnClickListener {
            (context as? Activity)?.startActivity<SearchActivity>(FROM_VIDEO_TAG to tag)
        }
        chip.setOnLongClickListener {
            tag.copyToClipboard()
            // todo: strings.xml
            showShortToast("「$tag」已複製到剪貼簿")
            return@setOnLongClickListener true
        }
        this.addView(chip)
    }
}

// base

internal val notificationManager by unsafeLazy { NotificationManagerCompat.from(applicationContext) }

internal fun getHanimeVideoLink(videoCode: String) = HANIME_BASE_URL + "watch?v=" + videoCode

// file

internal val hanimeVideoLocalFolder get() = applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

internal fun createDownloadName(title: String, quality: String) = "${title}_${quality}.mp4"

internal fun getDownloadedHanimeFile(title: String, quality: String): File {
    return File(hanimeVideoLocalFolder, createDownloadName(title, quality))
}

internal fun checkDownloadedHanimeFile(startsWith: String): Boolean {
    return hanimeVideoLocalFolder?.let { folder ->
        folder.listFiles()?.firstOrNull { it.name.startsWith(startsWith) } != null
    } ?: false
}

/**
 * Must be Activity Context!
 */
internal fun Context.openDownloadedHanimeVideoLocally(uri: String) {
    val videoFile = uri.toUri().toFile()
    val fileUri = FileProvider.getUriForFile(
        this, "com.yenaly.han1meviewer.fileProvider", videoFile
    )
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(fileUri, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}

// 務必保證v=後面都是數字！！一般在程式内特定環境使用，分析剪貼簿時不要用！！
internal fun String.toVideoCode() = substringAfter("watch?v=")

// log in and log out

internal fun logout() {
    alreadyLogin = false
    loginCookie = CookieString(EMPTY_STRING)
    cookieMap = null
    CookieManager.getInstance().removeAllCookies(null)
}

internal fun login(cookie: CookieString) {
    alreadyLogin = true
    loginCookie = cookie
}

// cookie!!

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
    val videoLanguage = preferenceSp.getString("video_language", "zh-CHT") ?: "zh-CHT"
    val videoLanguageCookie = Cookie.Builder().domain(domain)
        .name("user_lang")
        .value(videoLanguage)
        .build()
    return listOf(videoLanguageCookie)
}