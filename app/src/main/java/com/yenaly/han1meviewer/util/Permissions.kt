package com.yenaly.han1meviewer.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.yenaly.han1meviewer.R
import com.yenaly.yenaly_libs.utils.awaitActivityResult
import com.yenaly.yenaly_libs.utils.requestPermission
import com.yenaly.yenaly_libs.utils.requireComponentActivity
import com.yenaly.yenaly_libs.utils.showShortToast


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
    val dialog = requireComponentActivity().createAlertDialog {
        setTitle(R.string.allow_post_notification)
        setMessage(R.string.reason_for_download_notification)
    }
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
    val dialog = requireComponentActivity().createAlertDialog {
        setTitle(R.string.allow_install_from_unknown_app_sources)
        setMessage(R.string.reason_for_allow_install_from_unknown_app_sources)
    }
    return dialog.await(getString(R.string.go_to_settings), getString(R.string.deny))
}