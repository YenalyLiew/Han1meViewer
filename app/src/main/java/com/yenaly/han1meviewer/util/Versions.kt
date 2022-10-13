package com.yenaly.han1meviewer.util

import com.yenaly.yenaly_libs.utils.appLocalVersionCode

internal fun checkNeedUpdate(versionName: String): Boolean {
    val latestVersionCode = versionName.substringAfter("+", "0").toInt()
    return appLocalVersionCode < latestVersionCode
}

internal fun isPreReleaseVersion(versionName: String): Boolean {
    return "pre" in versionName
}