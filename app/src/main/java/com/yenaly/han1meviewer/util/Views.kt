package com.yenaly.han1meviewer.util

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Button.setDrawableTop(@DrawableRes drawableRes: Int) {
    this.setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0)
}

/**
 * 通过协程的方式异步加载布局
 */
suspend fun AsyncLayoutInflater.inflate(@LayoutRes resId: Int, parent: ViewGroup?): View =
    suspendCoroutine {
        inflate(resId, parent) { view, _, _ -> it.resume(view) }
    }