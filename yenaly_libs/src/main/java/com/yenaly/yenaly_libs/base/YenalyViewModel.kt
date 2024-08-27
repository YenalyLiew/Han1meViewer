package com.yenaly.yenaly_libs.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel

/**
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/20 020 11:37
 * @Description : Description...
 */
open class YenalyViewModel(
    @JvmField protected val application: Application
) : AndroidViewModel(application) {

//    private val singleFlowLaunch = SingleFlowLaunch()
//
//    protected fun CoroutineScope.singleLaunch(
//        context: CoroutineContext = EmptyCoroutineContext,
//        start: CoroutineStart = CoroutineStart.DEFAULT,
//        block: suspend CoroutineScope.() -> Unit
//    ) = singleFlowLaunch.singleLaunch(this, context, start, block)
}