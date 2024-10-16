package com.yenaly.yenaly_libs.utils

import com.google.gson.GsonBuilder
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

val gson by unsafeLazy {
    GsonBuilder().create()!!
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> String.fromJson(): T {
    return gson.fromJson(this, typeOf<T>().javaType)
}

fun Any?.toJson(): String {
    return gson.toJson(this)
}