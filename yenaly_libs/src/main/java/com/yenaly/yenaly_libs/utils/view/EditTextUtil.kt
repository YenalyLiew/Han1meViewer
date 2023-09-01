@file:JvmName("EditTextUtil")

package com.yenaly.yenaly_libs.utils.view

import android.view.Window
import android.widget.EditText
import com.yenaly.yenaly_libs.utils.SystemStatusUtil

/**
 * 在EditText上聚焦并显示软键盘
 *
 * @param window window
 */
fun EditText.showIme(window: Window) = this.let {
    it.isFocusable = true
    it.isFocusableInTouchMode = true
    if (it.hasFocus()) {
        SystemStatusUtil.showIme(window, true)
    } else {
        it.requestFocus()
        repeat(2) {
            SystemStatusUtil.showIme(window, true)
        }
    }
}

/**
 * 在EditText上解除聚焦并隐藏软键盘
 *
 * @param window window
 */
fun EditText.hideIme(window: Window) = this.let {
    it.clearFocus()
    SystemStatusUtil.showIme(window, false)
}