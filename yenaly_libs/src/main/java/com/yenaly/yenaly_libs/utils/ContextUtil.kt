@file:Suppress("unused")
@file:JvmName("ContextUtil")

package com.yenaly.yenaly_libs.utils

import android.app.Application
import android.content.Context

lateinit var applicationContext: Context
    @JvmSynthetic
    internal set

val application get() = applicationContext as Application