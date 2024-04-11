@file:JvmName("ViewUtil")
@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams

/**
 * 设置view的margins，设置相对位置margins更符合Google标准
 *
 * @param start  view的开始位
 * @param top    view的顶部
 * @param end    view的结束位
 * @param bottom view的底部
 */
fun View.setMargins(
    start: Int = marginStart,
    top: Int = marginTop,
    end: Int = marginEnd,
    bottom: Int = marginBottom,
) = updateLayoutParams<MarginLayoutParams> {
    setMargins(0, top, 0, bottom)
    marginStart = start
    marginEnd = end
}

fun View.resize(
    scaleX: Float,
    scaleY: Float,
) = updateLayoutParams {
    width = (width * scaleX).toInt()
    height = (height * scaleY).toInt()
}

/**
 * This function is used to remove a view from its parent ViewGroup.
 * It checks if the parent of the view is a ViewGroup and if so, removes the view from it.
 */
fun View.removeItself() {
    (parent as? ViewGroup)?.removeView(this)
}

/**
 * This is a generic function that finds and returns the parent of a view that is of a specific type.
 * It traverses up the view hierarchy until it finds a parent of the specified type.
 * If no parent of the specified type is found, it throws an error.
 *
 * @return The parent of the view that is of the specified type.
 * @throws Error if no parent of the specified type is found.
 */
inline fun <reified T : View> View.findParent(): T {
    var parent = parent
    while (parent != null) {
        if (parent is T) {
            return parent
        }
        parent = parent.parent
    }
    error("No parent of type ${T::class.java.simpleName} found")
}