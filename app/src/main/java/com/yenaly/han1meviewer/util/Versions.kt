package com.yenaly.han1meviewer.util

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.BuildConfig
import com.yenaly.han1meviewer.FILE_PROVIDER_AUTHORITY
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.worker.HUpdateWorker
import com.yenaly.yenaly_libs.utils.showShortToast
import java.io.File

fun checkNeedUpdate(versionName: String): Boolean {
    val latestVersionCode = versionName.substringAfter("+", "").toIntOrNull() ?: Int.MAX_VALUE
    return BuildConfig.VERSION_CODE < latestVersionCode
}

fun isPreReleaseVersion(versionName: String): Boolean {
    return "pre" in versionName
}

suspend fun Context.showUpdateDialog(latest: Latest) {
    val activity = this.toComponentActivity()
    val spannable = spannable {
        getString(R.string.new_version_found).span {
            style(Typeface.BOLD)
            relativeSize(1.2f)
        }
        newline()
        latest.version.text()
        newline()
        getString(R.string.update_content).span {
            style(Typeface.BOLD)
            relativeSize(1.2f)
        }
        newline()
        latest.changelog.text()
    }
    val dialog =
        MaterialAlertDialogBuilder(activity).setTitle(R.string.new_version_found)
            .setMessage(spannable)
            .setCancelable(false).create()
    val res = dialog.await(
        positiveText = getString(R.string.update),
        negativeText = getString(R.string.cancel),
    )
    if (res == AlertDialog.BUTTON_POSITIVE) {
        requestPostNotificationPermission()
        HUpdateWorker.enqueue(this, latest.downloadLink)
        showShortToast(R.string.update_download_background)
    }
    dialog.dismiss()
}

suspend fun Context.installApkPackage(file: File) {
    val canInstall = requestInstallPermission()
    if (canInstall) {
        val uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        startActivity(intent)
    }
}
