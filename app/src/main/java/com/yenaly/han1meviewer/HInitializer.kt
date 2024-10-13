package com.yenaly.han1meviewer

import android.content.Context
import com.yenaly.yenaly_libs.base.YenalyInitializer

class HInitializer : YenalyInitializer() {
    override fun create(context: Context) {
        super.create(context)
        // 用于处理 Firebase Crashlytics 初始化
        Thread.setDefaultUncaughtExceptionHandler(HCrashHandler)
    }
}