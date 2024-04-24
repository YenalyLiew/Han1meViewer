package com.yenaly.yenaly_libs

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.yenaly.yenaly_libs.utils.applicationContext
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

/**
 * @author Yenaly Liew
 * @time 2023/08/15 015 19:36
 */
@Suppress("unused")
object ActivityManager {

    private const val TAG = "ActivityManager"

    @JvmStatic
    var currentActivity: WeakReference<Activity?> = WeakReference(null)
        internal set

    @JvmStatic
    fun exit(killProcess: Boolean = true) {
        if (killProcess) exitProcess(0)
        Log.i(TAG, "exit")
    }

    @JvmStatic
    fun restart(killProcess: Boolean = true) {
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
        checkNotNull(intent) { "Intent is null" }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        applicationContext.startActivity(intent)
        if (killProcess) exitProcess(0)
    }
}