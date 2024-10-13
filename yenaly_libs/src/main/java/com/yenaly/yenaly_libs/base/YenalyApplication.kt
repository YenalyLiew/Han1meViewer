@file:Suppress("unused")

package com.yenaly.yenaly_libs.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.yenaly.yenaly_libs.ActivityManager
import com.yenaly.yenaly_libs.utils.isDebugEnabled
import java.lang.ref.WeakReference

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 21:52
 * @Description : Description...
 */
open class YenalyApplication : Application(), Application.ActivityLifecycleCallbacks {

    open val isDefaultCrashHandlerEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        // do not forget to register the crash dialog activity!
        if (isDefaultCrashHandlerEnabled && !isDebugEnabled) YenalyCrashHandler.instance.init(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        ActivityManager.currentActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}