package com.yenaly.yenaly_libs.utils

import com.google.gson.reflect.TypeToken

inline fun <reified T> fromJson(json: String): T {
    return GsonUtil.gson.fromJson(json, object : TypeToken<T>() {}.type)
}

@JvmName("stringFromJson")
inline fun <reified T> String.fromJson(): T {
    return GsonUtil.gson.fromJson(this, object : TypeToken<T>() {}.type)
}

fun Any?.toJson(): String {
    return GsonUtil.gson.toJson(this)
}