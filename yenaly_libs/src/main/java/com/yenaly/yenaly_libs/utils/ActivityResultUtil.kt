@file:JvmName("PermissionUtil")

package com.yenaly.yenaly_libs.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Thanks to https://github.com/FooIbar/EhViewer/

// 为了让权限申请更轻松，主要是为了能全局控制，
// 不得不使用了 ActivitiesManager.currentActivity 这个全局变量代替原有 Context
// 所以使用的时候务必注意！

private val atomicInteger = AtomicInteger()

suspend fun <I, O> Context.awaitActivityResult(
    contract: ActivityResultContract<I, O>,
    input: I,
): O {
    val key = "activity_rq#${atomicInteger.getAndIncrement()}"

    val activity = this.requireComponentActivity()
    val lifecycle = activity.lifecycle
    var launcher: ActivityResultLauncher<I>? = null
    val observer: LifecycleEventObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (Lifecycle.Event.ON_DESTROY === event) {
                launcher?.unregister()
                lifecycle.removeObserver(this)
            }
        }
    }

    return withContext(Dispatchers.Main) {
        lifecycle.addObserver(observer)
        suspendCoroutine { cont -> // No cancellation support here since we cannot cancel a launched Intent
            launcher = activity.activityResultRegistry.register(key, contract) {
                launcher?.unregister()
                lifecycle.removeObserver(observer)
                cont.resume(it)
            }.apply { launch(input) }
        }
    }
}

suspend fun Context.requestPermission(key: String): Boolean {
    if (ContextCompat.checkSelfPermission(
            this, key
        ) == PackageManager.PERMISSION_GRANTED
    ) return true
    return awaitActivityResult(ActivityResultContracts.RequestPermission(), key)
}