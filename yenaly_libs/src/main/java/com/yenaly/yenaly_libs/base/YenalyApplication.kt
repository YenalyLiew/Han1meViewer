@file:Suppress("unused")

package com.yenaly.yenaly_libs.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.yenaly.yenaly_libs.ActivitiesManager
import com.yenaly.yenaly_libs.utils.debuggable

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/16 016 21:52
 * @Description : Description...
 */
open class YenalyApplication : Application(), Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        // do not forget to register the crash dialog activity!
        if (!debuggable) YenalyCrashHandler.getInstance().init(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivitiesManager.pushActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        ActivitiesManager.popActivity(activity)
    }
}