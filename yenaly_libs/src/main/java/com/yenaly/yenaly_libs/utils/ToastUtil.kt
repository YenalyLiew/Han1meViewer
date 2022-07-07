@file:JvmName("ToastUtil")

package com.yenaly.yenaly_libs.utils

import android.widget.Toast

fun showShortToast(text: String?) {
    Toast.makeText(applicationContext, "$appName: $text", Toast.LENGTH_SHORT).show()
}

fun showLongToast(text: String?) {
    Toast.makeText(applicationContext, "$appName: $text", Toast.LENGTH_LONG).show()
}