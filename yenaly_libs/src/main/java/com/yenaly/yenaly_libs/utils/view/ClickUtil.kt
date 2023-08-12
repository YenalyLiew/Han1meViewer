@file:JvmName("ClickUtil")
@file:Suppress("unused")

package com.yenaly.yenaly_libs.utils.view

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(DelicateCoroutinesApi::class)
fun <E> SendChannel<E>.safeOffer(value: E) = !isClosedForSend && try {
    trySend(value).isSuccess
} catch (e: CancellationException) {
    false
}

fun View.clickFlow(): Flow<View> {
    return callbackFlow {
        setOnClickListener {
            safeOffer(it)
        }
        awaitClose { setOnClickListener(null) }
    }
}

/**
 * 带生命周期的click
 */
fun View.click(lifecycle: Lifecycle, onClick: View.OnClickListener) {
    clickFlow().onEach {
        onClick.onClick(this)
    }.launchIn(lifecycle.coroutineScope)
}

/**
 * 延迟点击
 */
fun View.clickDelayed(
    lifecycle: Lifecycle,
    delayMillis: Long = 500,
    onClick: View.OnClickListener
) {
    clickFlow().onEach {
        delay(delayMillis)
        onClick.onClick(this)
    }.launchIn(lifecycle.coroutineScope)
}

private var lastMillis: Long = 0

/**
 * 防止多次点击
 */
fun View.clickTrigger(
    lifecycle: Lifecycle,
    intervalMillis: Long = 500,
    onClick: View.OnClickListener
) {
    clickFlow().onEach {
        val currentMillis = System.currentTimeMillis()
        if (currentMillis - lastMillis < intervalMillis) {
            return@onEach
        }
        lastMillis = currentMillis
        onClick.onClick(this)
    }.launchIn(lifecycle.coroutineScope)
}