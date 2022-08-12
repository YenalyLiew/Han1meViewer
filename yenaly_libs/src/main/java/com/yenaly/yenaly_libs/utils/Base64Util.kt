@file:Suppress("unused")
@file:JvmName("Base64Util")

package com.yenaly.yenaly_libs.utils

import android.util.Base64

/**
 * Base64加密
 */
@JvmName("encodeToString")
fun String.encodeToStringByBase64(flag: Int = Base64.DEFAULT): String {
    return Base64.encodeToString(this.toByteArray(), flag)
}

/**
 * Base64解密
 */
@JvmName("decodeToString")
fun String.decodeToStringByBase64(flag: Int = Base64.DEFAULT): String {
    return String(Base64.decode(this.toByteArray(), flag))
}

/**
 * Base64解密
 */
@JvmName("decodeToByteArray")
fun String.decodeToByteArrayByBase64(flag: Int = Base64.DEFAULT): ByteArray {
    return Base64.decode(this.toByteArray(), flag)
}