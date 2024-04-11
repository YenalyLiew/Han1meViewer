@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.util.TypedValueCompat

/**
 * 通过dp获取相应px值
 */

val Number.dp: Int
    @JvmName("dpToPx")
    get() = TypedValueCompat.dpToPx(
        this.toFloat(),
        applicationContext.resources.displayMetrics
    ).toInt()

/**
 * 通过sp获取相应px值
 */
val Number.sp: Int
    @JvmName("spToPx")
    get() = TypedValueCompat.spToPx(
        this.toFloat(),
        applicationContext.resources.displayMetrics
    ).toInt()

/**
 * 获取本地储存状态栏高度px
 */
val statusBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    get() {
        val resources: Resources = applicationContext.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

/**
 * 获取本地储存导航栏高度px
 */
val navBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    get() {
        val resources: Resources = applicationContext.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

/**
 * 判断当前是否横屏
 */
val isOrientationLandscape: Boolean
    get() {
        return applicationContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }