package com.yenaly.han1meviewer.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.showShortToast
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

private suspend fun <I, O> Context.awaitActivityResult(
    contract: ActivityResultContract<I, O>,
    input: I,
): O {
    val key = "activity_rq#${atomicInteger.getAndIncrement()}"

    val activity = this.toComponentActivity()
    val lifecycle = activity.lifecycle
    var launcher: ActivityResultLauncher<I>? = null
    var observer: LifecycleEventObserver? = null
    observer = LifecycleEventObserver { _, event ->
        if (Lifecycle.Event.ON_DESTROY == event) {
            launcher?.unregister()
            if (observer != null) {
                lifecycle.removeObserver(observer!!)
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

private suspend fun Context.requestPermission(key: String): Boolean {
    if (ContextCompat.checkSelfPermission(
            this, key
        ) == PackageManager.PERMISSION_GRANTED
    ) return true
    return awaitActivityResult(ActivityResultContracts.RequestPermission(), key)
}

/**
 * 请求选择图片或视频
 */
suspend fun Context.pickVisualMedia(type: ActivityResultContracts.PickVisualMedia.VisualMediaType): Uri? =
    awaitActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        PickVisualMediaRequest.Builder().setMediaType(type).build()
    )

/**
 * 獲得發送通知權限
 */
suspend fun Context.requestPostNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = requestPermission(Manifest.permission.POST_NOTIFICATIONS)
        if (!granted) {
            val res = showPostNotificationPermissionDialog()
            if (res == AlertDialog.BUTTON_NEGATIVE) {
                showShortToast(R.string.msg_deny_download_notification)
                return false
            }
            requestPermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    return true
}

/**
 * 顯示發送通知權限對話框
 */
private suspend fun Context.showPostNotificationPermissionDialog(): Int {
    val dialog = MaterialAlertDialogBuilder(this.toComponentActivity())
        .setTitle(R.string.allow_post_notification)
        .setMessage(R.string.reason_for_download_notification)
        .create()
    return dialog.await(getString(R.string.allow), getString(R.string.deny))
}

/**
 * 请求安装权限
 */
suspend fun Context.requestInstallPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (packageManager.canRequestPackageInstalls()) return true
        val granted = requestPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        if (!granted) {
            val res = showInstallPermissionDialog()
            if (res == AlertDialog.BUTTON_NEGATIVE) return false
            awaitActivityResult(
                ActivityResultContracts.StartActivityForResult(),
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:$packageName"),
                ),
            )
            requestPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }
        return packageManager.canRequestPackageInstalls()
    }
    return true
}

/**
 * 显示安装权限对话框
 */
private suspend fun Context.showInstallPermissionDialog(): Int {
    val dialog = MaterialAlertDialogBuilder(this.toComponentActivity())
        .setTitle(R.string.allow_install_from_unknown_app_sources)
        .setMessage(R.string.reason_for_allow_install_from_unknown_app_sources)
        .create()
    return dialog.await(getString(R.string.go_to_settings), getString(R.string.deny))
}