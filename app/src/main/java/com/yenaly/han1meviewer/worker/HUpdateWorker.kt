package com.yenaly.han1meviewer.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.UPDATE_NOTIFICATION_CHANNEL
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.logic.network.HUpdater
import com.yenaly.han1meviewer.util.installApkPackage
import com.yenaly.han1meviewer.util.runSuspendCatching
import com.yenaly.han1meviewer.util.updateFile
import com.yenaly.yenaly_libs.utils.showShortToast
import kotlin.random.Random

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/22 022 21:27
 */
class HUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), WorkerMixin {
    companion object {
        const val TAG = "HUpdateWorker"

        const val DOWNLOAD_LINK = "download_link"
        const val NODE_ID = "node_id"
        const val UPDATE_APK = "update_apk"

        /**
         * This function is used to enqueue a download task
         */
        fun enqueue(context: Context, latest: Latest) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val data = workDataOf(
                DOWNLOAD_LINK to latest.downloadLink,
                NODE_ID to latest.nodeId,
            )
            val req = OneTimeWorkRequestBuilder<HUpdateWorker>()
                .addTag(TAG)
                .setConstraints(constraints)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context)
                .beginUniqueWork(TAG, ExistingWorkPolicy.REPLACE, req)
                .enqueue()
        }

        /**
         * This function is used to collect the output of the download task
         */
        suspend fun collectOutput(context: Context) = WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(TAG)
            .collect { workInfos ->
                // 只有一個！
                val workInfo = workInfos.firstOrNull()
                workInfo?.let {
                    when (it.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val apkPath = it.outputData.getString(UPDATE_APK)
                            val file = apkPath?.toUri()?.toFile()
                            file?.let { context.installApkPackage(file) }
                        }

                        WorkInfo.State.FAILED -> {
                            showShortToast(R.string.update_failed)
                        }

                        else -> Unit
                    }
                }
            }
    }

    private val downloadLink by inputData(DOWNLOAD_LINK, EMPTY_STRING)
    private val nodeId by inputData(NODE_ID, EMPTY_STRING)
    private val downloadId = Random.nextInt()

    override suspend fun doWork(): Result {
        with(HUpdater) {
            val file = context.updateFile.apply { delete() }
            val inject = runSuspendCatching {
                setForeground(createForegroundInfo(progress = 0))
                file.injectUpdate(downloadLink) { progress ->
                    setForeground(createForegroundInfo(progress))
                }
            }
            if (inject.isSuccess) {
                val outputData = workDataOf(UPDATE_APK to file.toUri().toString())
                Preferences.updateNodeId = nodeId
                return Result.success(outputData)
            } else {
                inject.exceptionOrNull()?.printStackTrace()
                file.delete()
                return Result.failure()
            }
        }
    }

    private fun createForegroundInfo(progress: Int = 0): ForegroundInfo {
        return ForegroundInfo(
            downloadId,
            NotificationCompat.Builder(context, UPDATE_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentTitle(context.getString(R.string.downloading_update_percent, progress))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(true)  // #issue-98: 下载时不发出声音
                .setProgress(100, progress, false)
                .build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
        )
    }
}