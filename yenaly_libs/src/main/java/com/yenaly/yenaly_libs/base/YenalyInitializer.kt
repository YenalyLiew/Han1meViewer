@file:Suppress("unused")

package com.yenaly.yenaly_libs.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.startup.Initializer
import com.yenaly.yenaly_libs.utils.applicationContext

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/21 021 14:04
 * @Description : Description...
 */
open class YenalyInitializer : Initializer<Unit> {

    @CallSuper
    override fun create(context: Context) {
        applicationContext = context
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}