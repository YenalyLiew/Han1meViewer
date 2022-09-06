@file:Suppress("unused")
@file:JvmName("ContextUtil")

package com.yenaly.yenaly_libs.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper

@set:JvmSynthetic
lateinit var applicationContext: Context
    internal set

val application get() = applicationContext as Application


val Context.activity: Activity?
    get() {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }