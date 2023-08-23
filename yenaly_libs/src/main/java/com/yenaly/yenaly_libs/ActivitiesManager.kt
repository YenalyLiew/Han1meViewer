package com.yenaly.yenaly_libs

import android.app.Activity
import android.content.Intent
import android.os.Process
import android.util.Log
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @author Yenaly Liew
 * @time 2023/08/15 015 19:36
 */
@Suppress("unused")
object ActivitiesManager {

    private const val TAG = "ActivitiesManager"

    private val activities = linkedSetOf<Activity>()

    @JvmStatic
    val currentActivity: Activity? get() = activities.lastOrNull()

    @JvmStatic
    fun push(activity: Activity) {
        if (activity in activities) {
            activities.remove(activity)
            activities.add(activity)
        } else activities.add(activity)
        Log.i(TAG, "push: ${activity.javaClass.simpleName}")
    }

    @JvmStatic
    fun remove(activity: Activity) {
        activities.remove(activity)
        Log.i(TAG, "pop: ${activity.javaClass.simpleName}")
    }

    @JvmStatic
    fun finish(clazz: Class<*>) {
        for (activity in activities) {
            if (activity.javaClass == clazz) {
                activity.finish()
                Log.i(TAG, "finish: ${activity.javaClass.simpleName}")
            }
        }
    }

    @JvmStatic
    fun finish(activity: Activity) {
        activity.finish()
        Log.i(TAG, "finish: ${activity.javaClass.simpleName}")
    }

    @JvmStatic
    @JvmOverloads
    fun finishOther(current: Activity? = null) {
        (current ?: currentActivity)?.let { cur ->
            for (activity in activities) {
                if (activity != cur) {
                    activity.finish()
                    Log.i(TAG, "finishOther: ${activity.javaClass.simpleName}")
                }
            }
        }
    }

    @JvmStatic
    fun finishAll() {
        for (activity in activities) {
            activity.finish()
            Log.i(TAG, "finishAll: ${activity.javaClass.simpleName}")
        }
    }

    @JvmStatic
    fun recreateAll() {
        for (activity in activities) {
            activity.recreate()
            Log.i(TAG, "recreateAll: ${activity.javaClass.simpleName}")
        }
    }

    @JvmStatic
    fun exit(killProcess: Boolean = true) {
        finishAll()
        if (killProcess) Process.killProcess(Process.myPid())
        Log.i(TAG, "exit")
    }

    @JvmStatic
    fun restart(killProcess: Boolean = true) {
        finishAll()
        val intent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
        checkNotNull(intent) { "Intent is null" }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        applicationContext.startActivity(intent)
        if (killProcess) Process.killProcess(Process.myPid())
    }
}