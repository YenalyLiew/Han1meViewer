package com.yenaly.yenaly_libs.utils

import android.util.Log
import com.yenaly.yenaly_libs.BuildConfig

fun <T : Any> logFieldsChange(tag: String, oldItem: T, newItem: T) {
    if (!BuildConfig.DEBUG) return

    val oldFields = oldItem::class.java.declaredFields
    val newFields = newItem::class.java.declaredFields

    for (i in oldFields.indices) {
        oldFields[i].isAccessible = true
        newFields[i].isAccessible = true
        val oldValue = oldFields[i].get(oldItem)
        val newValue = newFields[i].get(newItem)
        if (oldValue != newValue) {
            Log.i(tag, "Field ${oldFields[i].name} changed from $oldValue to $newValue")
        }
    }
}