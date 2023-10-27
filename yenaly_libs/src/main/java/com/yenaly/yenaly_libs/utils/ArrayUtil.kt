@file:JvmName("ArrayUtil")

package com.yenaly.yenaly_libs.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.stream.Stream

/**
 * 将 IntArray 转化为 StringArray，不经过 List
 */
fun IntArray.toStringArray(radix: Int = 10): Array<String> {
    return Array(size) { get(it).toString(radix) }
}

/**
 * 将 LongArray 转化为 StringArray，不经过 List
 */
fun LongArray.toStringArray(radix: Int = 10): Array<String> {
    return Array(size) { get(it).toString(radix) }
}

/**
 * 将 LongArray 转化为 StringArray，不经过 List
 */
fun FloatArray.toStringArray(): Array<String> {
    return Array(size) { get(it).toString() }
}

/**
 * 将 DoubleArray 转化为 StringArray，不经过 List
 */
fun DoubleArray.toStringArray(): Array<String> {
    return Array(size) { get(it).toString() }
}

/**
 * Kotlin 专属 Stream toArray 语法糖。
 */
@RequiresApi(Build.VERSION_CODES.N)
inline fun <reified T> Stream<*>.toTypedArray(): Array<T?> =
    toArray { size -> arrayOfNulls<T>(size) }
