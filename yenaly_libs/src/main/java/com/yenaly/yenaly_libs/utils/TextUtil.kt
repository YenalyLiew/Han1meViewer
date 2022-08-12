@file:JvmName("TextUtil")

package com.yenaly.yenaly_libs.utils

import android.text.format.Formatter
import java.util.*

/**
 * 把一个秒数转化为时间格式，常用于视频持续时间。
 *
 * 例如：123(s) -> 02:03
 *
 * @author Yenaly Liew
 */
fun Long.secondToTimeCase(): String {
    val second: Long = this % 60
    var minute: Long = this / 60
    var hour = 0L
    if (minute >= 60) {
        hour = minute / 60
        minute %= 60
    }
    val secondString = if (second < 10) "0$second" else second.toString()
    val minuteString = if (minute < 10) "0$minute" else minute.toString()
    val hourString = if (hour < 10) "0$hour" else hour.toString()
    return if (hour != 0L) "$hourString:$minuteString:$secondString" else "$minuteString:$secondString"
}

/**
 * 把一个数转化为带万、亿的格式，常用于视频播放量等。
 *
 * 例如：102002 -> 10.2万
 *
 * @author Yenaly Liew
 */
fun Long.formatPlayCount(): String {
    return when {
        this < 0 -> "0"
        this < 1_0000 -> this.toString()
        this < 1_0000_0000 -> String.format(
            Locale.getDefault(),
            "%d.%02d万",
            this / 1_0000,
            this % 1_0000 / 100
        )
        else -> String.format(
            Locale.getDefault(),
            "%d.%02d亿",
            this / 1_0000_0000,
            this % 1_0000_0000 / 100_0000
        )
    }
}

fun Long.formatFileSize(short: Boolean = true): String {
    if (short) {
        return Formatter.formatShortFileSize(applicationContext, this)
    }
    return Formatter.formatFileSize(applicationContext, this)
}

fun String?.isInt(): Boolean {
    if (this == null) return false
    return try {
        toInt()
        true
    } catch (e: NumberFormatException) {
        false
    }
}