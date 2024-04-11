@file:JvmName("AppUtil")

package com.yenaly.yenaly_libs.utils

import android.content.pm.ApplicationInfo
import androidx.core.content.pm.PackageInfoCompat

/**
 * 获取APP名称
 */
val appName: String
    get() = applicationContext.applicationInfo
        .loadLabel(applicationContext.packageManager).toString()

/**
 * 获取本地APP版本号，获取失败则返回null
 *
 * @return 版本号，例如 1.0.0
 */
val appLocalVersionName: String
    get() {
        return applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName, 0
        ).versionName
    }

/**
 * 获取本地APP版本代码，获取失败则返回0
 *
 * @return 版本代码，例如 12314355
 */
val appLocalVersionCode: Long
    get() {
        val packageInfo = applicationContext.packageManager.getPackageInfo(
            applicationContext.packageName, 0
        )
        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

/**
 * 获取APP可及屏幕宽度
 */
val appScreenWidth: Int get() = applicationContext.resources.displayMetrics.widthPixels

/**
 * 获取APP可及屏幕高度
 */
val appScreenHeight: Int get() = applicationContext.resources.displayMetrics.heightPixels

/**
 * 判断当前是否为DEBUG模式
 *
 * @return true if debuggable
 */
val isDebugEnabled: Boolean
    get() = 0 != applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE