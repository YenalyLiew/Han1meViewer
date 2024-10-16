@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils

import android.content.res.Configuration
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 取消当前透明状态栏，再加一个带颜色的状态栏
 *
 * @param color 想要的状态栏颜色
 */
@Suppress("DEPRECATION")
fun Window.addStatusBarWithColor(@ColorInt color: Int) {
    //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = color
}

/**
 * 获取当前实际状态栏高度
 */
val Window.currentStatusBarHeight: Int
    get() {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
        return windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
            ?: statusBarHeight
    }

/**
 * 获取当前实际导航栏高度
 */
val Window.currentNavBarHeight: Int
    get() {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
        return windowInsetsCompat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom
            ?: navBarHeight
    }

/**
 * 当前状态栏是否可见
 */
val Window.isStatusBarVisible: Boolean
    get() {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
        return windowInsetsCompat?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
    }

/**
 * 当前导航栏是否可见
 */
val Window.isNavBarVisible: Boolean
    get() {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
        return windowInsetsCompat?.isVisible(WindowInsetsCompat.Type.navigationBars()) ?: true
    }

/**
 * 控制状态栏是否可见
 */
fun Window.controlStatusBar(isVisible: Boolean) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    if (isVisible) {
        controller.show(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }
}

/**
 * 控制导航栏是否可见
 */
fun Window.controlNavBar(isVisible: Boolean) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    if (isVisible) {
        controller.show(WindowInsetsCompat.Type.navigationBars())
    } else {
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

/**
 * 控制系统栏（包含状态栏，导航栏）是否可见
 */
fun Window.controlSystemBars(isVisible: Boolean) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    if (isVisible) {
        controller.show(WindowInsetsCompat.Type.systemBars())
    } else {
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * 是否将系统栏图标切换成亮色模式
 *
 * @param statusBar 设置状态栏图标为亮色模式
 * @param navBar    设置导航栏图标为亮色模式
 */
fun Window.setSystemBarIconLightMode(statusBar: Boolean, navBar: Boolean = false) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    controller.isAppearanceLightStatusBars = statusBar
    controller.isAppearanceLightNavigationBars = navBar
}

/**
 * 当前软键盘是否可见
 *
 * @return 是否可见
 */
inline val Window.isImeVisible: Boolean
    get() {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
        return windowInsetsCompat?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
    }

/**
 * 是否显示软键盘
 *
 * @param ime    是否显示软键盘
 */
fun Window.showIme(ime: Boolean) {
    val controller = WindowCompat.getInsetsController(this, decorView)
    if (ime) {
        controller.show(WindowInsetsCompat.Type.ime())
    } else {
        controller.hide(WindowInsetsCompat.Type.ime())
    }
}

/**
 * 当前是否为夜间模式
 */
val isAppDarkMode: Boolean
    get() {
        return applicationContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

/**
 * Sets whether the decor view should fit root-level content views for
 * {@link WindowInsetsCompat}.
 * <p>
 * If set to {@code false}, the framework will not fit the content view to the insets and will
 * just pass through the {@link WindowInsetsCompat} to the content view.
 * </p>
 * <p>
 * Please note: using the {@link View#setSystemUiVisibility(int)} API in your app can
 * conflict with this method. Please discontinue use of {@link View#setSystemUiVisibility(int)}.
 * </p>
 *
 * @param decorFitsSystemWindows Whether the decor view should fit root-level content views for
 *                               insets.
 */
fun Window.isDecorFitsSystemWindows(decorFitsSystemWindows: Boolean) =
    WindowCompat.setDecorFitsSystemWindows(this, decorFitsSystemWindows)