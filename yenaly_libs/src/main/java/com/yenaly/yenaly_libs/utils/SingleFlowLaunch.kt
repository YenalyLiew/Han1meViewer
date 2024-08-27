package com.yenaly.yenaly_libs.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * For ViewModelScope.
 * Avoid triggering request multiple times when activity recreates and
 * you call `viewModelScope.launch` function in activity or fragment.
 *
 * For example:
 * ```kotlin
 * val singleFlowLaunch = SingleFlowLaunch()
 *
 * fun foo(param: Any) {
 *     singleFlowLaunch.singleLaunch(viewModelScope, UNIQUE_TAG) {
 *         Repository.suspendFunction(param)
 *     }
 * }
 * ```
 *
 * @author Yenaly Liew
 * @time 2022/07/17 017 22:34
 */
@Deprecated("totally trash")
class SingleFlowLaunch {

    private val jobMap = mutableMapOf<SuspendCoroutineScopeBlock, AtomicInteger>()

    /**
     * Single [CoroutineScope.launch] only for ViewModelScope,
     * avoid triggering request multiple times when activity recreates and
     * you call `viewModelScope.launch` function in activity or fragment.
     *
     * If you use [SharedFlow][kotlinx.coroutines.flow.SharedFlow] with [singleLaunch],
     * you should set the param `replay` to 1 or higher to cache the latest data.
     * Otherwise, you might not get the data.
     */
    fun singleLaunch(
        viewModelScope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: SuspendCoroutineScopeBlock,
    ): Job? {
        if (jobMap[block] == null) {
            jobMap[block] = AtomicInteger(0)
        }
        jobMap[block]!!.let { int ->
            if (int.getAndIncrement() != 0) {
                return null
            } else {
                return viewModelScope.launch(context, start, block)
            }
        }
    }
}

typealias SuspendCoroutineScopeBlock = suspend CoroutineScope.() -> Unit