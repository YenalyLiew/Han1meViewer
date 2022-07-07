@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils

/**
 * 通过dp获取相应px值
 */
inline val Number.dp get() = DeviceUtil.dp2px(this.toFloat())

/**
 * 通过sp获取相应px值
 */
inline val Number.sp get() = DeviceUtil.sp2px(this.toFloat())

/**
 * 获取本地储存状态栏高度px
 */
inline val statusBarHeight get() = DeviceUtil.getStatusBarHeight(applicationContext)

/**
 * 获取本地储存导航栏高度px
 */
inline val navBarHeight get() = DeviceUtil.getNavigationBarHeight(applicationContext)

/**
 * 判断当前是否横屏
 */
inline val isOrientationLandscape get() = DeviceUtil.isOrientationLandscape(applicationContext)