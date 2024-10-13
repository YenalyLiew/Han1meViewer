@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.util.TypedValueCompat

val Number.dpF: Float
    @JvmName("dpToPxF")
    get() = TypedValueCompat.dpToPx(
        this.toFloat(),
        applicationContext.resources.displayMetrics
    )

val Number.spF: Float
    @JvmName("spToPxF")
    get() = TypedValueCompat.spToPx(
        this.toFloat(),
        applicationContext.resources.displayMetrics
    )

/**
 * 通过dp获取相应px值
 */
val Number.dp: Int
    @JvmName("dpToPx")
    get() {
        val f = dpF
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        return res
    }

/**
 * 通过sp获取相应px值
 */
val Number.sp: Int
    @JvmName("spToPx")
    get() {
        val f = spF
        val res = (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
        return res
    }

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