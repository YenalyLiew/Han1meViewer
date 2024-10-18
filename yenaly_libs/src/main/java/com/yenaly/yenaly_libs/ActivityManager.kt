package com.yenaly.yenaly_libs

import android.annotation.SuppressLint
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

    private val topActivityByReflect: Activity?
        @SuppressLint("PrivateApi")
        get() {
            try {
                val activityThreadCls = Class.forName("android.app.ActivityThread")
                val activityThread = activityThreadCls.getMethod("currentActivityThread")(null)
                val activitiesField = activityThreadCls.getDeclaredField("mActivities")
                activitiesField.isAccessible = true
                val activities = activitiesField[activityThread] as Map<*, *>?
                if (activities == null) return null
                activities.values.forEach { activityRecord ->
                    val activityRecordCls = activityRecord?.javaClass
                    activityRecordCls?.getDeclaredField("paused")?.also { pausedField ->
                        pausedField.isAccessible = true
                        if (!pausedField.getBoolean(activityRecord)) {
                            val activityField = activityRecordCls.getDeclaredField("activity")
                            activityField.isAccessible = true
                            return activityField[activityRecord] as Activity
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

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