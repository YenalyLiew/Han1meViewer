@file:JvmName("ToastUtil")

package com.yenaly.yenaly_libs.utils

import android.widget.Toast
import androidx.annotation.StringRes

fun showShortToast(text: String?) {
    Toast.makeText(applicationContext, "$text", Toast.LENGTH_SHORT).show()
}

fun showLongToast(text: String?) {
    Toast.makeText(applicationContext, "$text", Toast.LENGTH_LONG).show()
}

fun showShortToast(@StringRes text: Int) {
    Toast.makeText(
        applicationContext,
        applicationContext.getString(text),
        Toast.LENGTH_SHORT
    ).show()
}

fun showLongToast(@StringRes text: Int) {
    Toast.makeText(
        applicationContext,
        applicationContext.getString(text),
        Toast.LENGTH_LONG
    ).show()
}