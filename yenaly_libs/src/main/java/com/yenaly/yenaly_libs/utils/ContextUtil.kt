@file:Suppress("unused")
@file:JvmName("ContextUtil")

package com.yenaly.yenaly_libs.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

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

inline fun <reified T : Activity> Context.findActivity(): T {
    var context = this
    while (context is ContextWrapper) {
        if (context is T) {
            return context
        }
        context = context.baseContext
    }
    error("No activity of type ${T::class.java.simpleName} found")
}

val Context.lifecycle: Lifecycle
    get() {
        var context: Context? = this
        while (true) {
            when (context) {
                is LifecycleOwner -> return context.lifecycle
                !is ContextWrapper -> error("This should never happen!")
                else -> context = context.baseContext
            }
        }
    }