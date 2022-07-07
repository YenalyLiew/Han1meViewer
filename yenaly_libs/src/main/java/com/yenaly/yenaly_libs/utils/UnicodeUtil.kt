@file:JvmName("UnicodeUtil")

package com.yenaly.yenaly_libs.utils

/**
 * 将字符串转成Unicode编码，包括但不限于中文
 *
 * @param src 原始字符串，包括但不限于中文
 * @return Unicode编码字符串
 */
@JvmName("decode")
fun stringDecodeToUnicode(src: String): String {
    val builder = StringBuilder()
    for (element in src) {
        // 如果你的Kotlin版本低于1.5，这里 element.code 会报错 找不到方法,请替换成:
        // Kotlin < 1.5
        // var s = Integer.toHexString(element.toInt())
        // Kotlin >= 1.5
        var s = Integer.toHexString(element.code)

        if (s.length == 2) {// 英文转16进制后只有两位，补全4位
            s = "00$s"
        }
        builder.append("\\u$s")
    }
    return builder.toString()
}

/**
 * 解码Unicode字符串，得到原始字符串
 *
 * @param unicode Unicode字符串
 * @return 解码后的原始字符串
 */
@JvmName("encode")
fun unicodeEncodeToString(unicode: String): String {
    val builder = StringBuilder()
    val hex = unicode.split("\\\\u".toRegex()).toTypedArray()
    for (i in 1 until hex.size) {
        val data = hex[i].toInt(16)
        builder.append(data.toChar())
    }
    return builder.toString()
}