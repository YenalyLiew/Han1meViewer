@file:JvmName("ArrayUtil")

package com.yenaly.yenaly_libs.utils

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
inline fun <reified T> Stream<*>.toTypedArray(): Array<T?> =
    toArray { size -> arrayOfNulls<T>(size) }

/**
 * 将 List 直接转化为 Array
 */
inline fun <I, reified O> List<I>.mapToArray(transform: (I) -> O): Array<O> {
    return Array(size) { i -> transform(this[i]) }
}
