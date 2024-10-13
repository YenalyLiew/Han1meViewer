package com.yenaly.han1meviewer

import com.yenaly.yenaly_libs.ActivityManager

object HCrashHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        ActivityManager.restart(killProcess = true)
    }
}