package com.yenaly.yenaly_libs.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.yenaly.yenaly_libs.ActivityManager
import com.yenaly.yenaly_libs.base.dialog.YenalyCrashDialogActivity
import com.yenaly.yenaly_libs.utils.applicationContext
import com.yenaly.yenaly_libs.utils.startActivity
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.concurrent.thread

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/21 021 21:15
 * @Description : Description...
 */
class YenalyCrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private lateinit var mContext: Context
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        @Volatile
        private var yenalyCrashHandler: YenalyCrashHandler? = null

        @JvmStatic
        fun getInstance(): YenalyCrashHandler {
            if (yenalyCrashHandler == null) {
                synchronized(YenalyCrashHandler::class.java) {
                    if (yenalyCrashHandler == null) {
                        yenalyCrashHandler = YenalyCrashHandler()
                    }
                }
            }
            return yenalyCrashHandler!!
        }
    }

    fun init(context: Context) {
        this.mContext = context
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (!handleError(e) && mDefaultHandler != null) {
            mDefaultHandler!!.uncaughtException(t, e)
        } else {
            val errorWriter = StringWriter()
            e.printStackTrace(PrintWriter(errorWriter))
            applicationContext.startActivity<YenalyCrashDialogActivity>(
                flag = Intent.FLAG_ACTIVITY_NEW_TASK,
                values = arrayOf("yenaly_throwable" to errorWriter.toString())
            )
            ActivityManager.exit(killProcess = true)
        }
    }

    private fun handleError(throwable: Throwable?): Boolean {
        if (throwable == null) {
            return false
        }

        thread {
            Looper.prepare()
            // TODO
            Looper.loop()
        }
        return true
    }
}