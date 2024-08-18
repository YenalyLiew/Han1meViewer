package com.yenaly.yenaly_libs.utils

import android.annotation.SuppressLint
import android.os.Build
import android.os.LocaleList
import java.util.Locale

@SuppressLint("ObsoleteSdkInt")
object LanguageHelper {

    val preferredLanguage: Locale
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }

    val Locale.isChinese: Boolean
        get() = language == "zh"

    val Locale.isSimplifiedChinese: Boolean
        get() = language == "zh" && country == "CN"

    val Locale.isTraditionalChinese: Boolean
        get() = language == "zh" && country == "TW"

    val Locale.isEnglish: Boolean
        get() = language == "en"
}