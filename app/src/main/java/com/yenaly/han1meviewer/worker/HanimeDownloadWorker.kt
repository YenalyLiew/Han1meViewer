package com.yenaly.han1meviewer.worker

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yenaly.han1meviewer.DOWNLOAD_NOTIFICATION_CHANNEL
import com.yenaly.han1meviewer.EMPTY_STRING
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.network.ServiceCreator
import com.yenaly.han1meviewer.util.DEF_VIDEO_TYPE
import com.yenaly.han1meviewer.util.await
import com.yenaly.han1meviewer.util.getDownloadedHanimeFile
import com.yenaly.yenaly_libs.utils.showShortToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.RandomAccessFile
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 11:42
 */
class HanimeDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), WorkerMixin {

    companion object {
        const val TAG = "HanimeDownloadWorker"

        const val RESPONSE_INTERVAL = 500L

        const val BACKOFF_DELAY = 10_000L

        const val DELETE = "delete"
        const val QUALITY = "quality"
        const val DOWNLOAD_URL = "download_url"
        const val VIDEO_TYPE = "video_type"
        const val HANIME_NAME = "hanime_name"
        const val VIDEO_CODE = "video_code"
        const val COVER_URL = "cover_url"
        const val REDOWNLOAD = "redownload"
        // const val RELEASE_DATE = "release_date"
        // const val COVER_DOWNLOAD = "cover_download"

        const val PROGRESS = "progress"
        const val FAILED_REASON = "failed_reason"

        /**
         * 方便统一管理下载 Worker 的创建
         */
        inline fun build(
            action: OneTimeWorkRequest.Builder.() -> Unit = {}
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
                .addTag(TAG)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    BACKOFF_DELAY, TimeUnit.MILLISECONDS
                ).apply(action).build()
        }

        /**
         * This function is used to collect the output of the download task
         */
        suspend fun collectOutput(context: Context) {
            WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(TAG)
                .collect { workInfos ->
                    workInfos.forEach { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.FAILED -> {
                                val err =
                                    workInfo.outputData.getString(FAILED_REASON)
                                err?.let {
                                    showShortToast(it)
                                }
                            }

                            else -> Unit
                        }
                    }
                }
        }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val hanimeName by inputData(HANIME_NAME, EMPTY_STRING)
    private val downloadUrl by inputData(DOWNLOAD_URL, EMPTY_STRING)
    private val videoType by inputData(VIDEO_TYPE, DEF_VIDEO_TYPE)
    private val quality by inputData(QUALITY, EMPTY_STRING)
    private val videoCode by inputData(VIDEO_CODE, EMPTY_STRING)
    private val coverUrl by inputData(COVER_URL, EMPTY_STRING)

    private val shouldDelete by inputData(DELETE, false)
    private val shouldRedownload by inputData(REDOWNLOAD, false)

    private val downloadId = Random.nextInt()
    private val successId = Random.nextInt()
    private val failId = Random.nextInt()

    override suspend fun doWork(): Result {
        if (shouldDelete) return Result.success()
        if (runAttemptCount > 2) {
            return Result.failure(
                workDataOf(
                    FAILED_REASON to context.getString(
                        R.string.download_s_failed_many_times, hanimeName
                    )
                )
            )
        } else if (runAttemptCount > 0) {
            Handler(Looper.getMainLooper()).post {
                // #issue-crashlytics-e2c7b3bb39a096026b99d4d90861f09a:
                // 他就是可能为空，但是我不知道为什么会为空
                @Suppress("USELESS_ELVIS")
                showShortToast(
                    context.getString(
                        R.string.download_failed_d_times_and_retry_s,
                        runAttemptCount, hanimeName ?: EMPTY_STRING
                    )
                )
            }
        }
        setForeground(createForegroundInfo())
        return download()
    }

    private suspend fun createNewRaf() {
        return withContext(Dispatchers.IO) {
            val file = getDownloadedHanimeFile(hanimeName, quality, suffix = videoType)
            var raf: RandomAccessFile? = null
            var response: Response? = null
            var body: ResponseBody? = null
            try {
                raf = RandomAccessFile(file, "rwd")
                val request = Request.Builder().url(downloadUrl).get().build()
                response = ServiceCreator.okHttpClient.newCall(request).await()
                if (response.isSuccessful) {
                    body = response.body
                    body?.let {
                        val len = body.contentLength()
                        if (len > 0) {
                            raf.setLength(len)
                            val entity = HanimeDownloadEntity(
                                coverUrl = coverUrl, title = hanimeName,
                                addDate = System.currentTimeMillis(), videoCode = videoCode,
                                videoUri = file.toUri().toString(), quality = quality,
                                videoUrl = downloadUrl, length = len, downloadedLength = 0,
                                isDownloading = false
                            )
                            DatabaseRepo.HanimeDownload.insert(entity)
                        }
                    }
                }
            } catch (_: Exception) {
                // 创建，但是并没有下载接收到文件大小，删除文件
                if (file.exists() && file.length() == 0L) file.delete()
            } finally {
                raf?.close()
                response?.close()
                body?.close()
            }
        }
    }

    private suspend fun download(): Result {
        return withContext(Dispatchers.IO) {
            val file = getDownloadedHanimeFile(hanimeName, quality, suffix = videoType)
            if (shouldRedownload) {
                DatabaseRepo.HanimeDownload.deleteBy(videoCode, quality)
                if (file.exists()) {
                    file.delete()
                }
            }
            val isExist = DatabaseRepo.HanimeDownload.isExist(videoCode, quality)
            if (!isExist) createNewRaf()
            val entity = DatabaseRepo.HanimeDownload.findBy(videoCode, quality)
                ?: return@withContext Result.retry()

            val needRange = entity.downloadedLength > 0
            var raf: RandomAccessFile? = null
            var response: Response? = null
            var body: ResponseBody? = null
            try {
                raf = RandomAccessFile(file, "rwd")
                val request = Request.Builder().url(downloadUrl)
                    .also { if (needRange) it.header("Range", "bytes=${entity.downloadedLength}-") }
                    .get().build()
                response = ServiceCreator.okHttpClient.newCall(request).await()
                entity.isDownloading = true
                raf.seek(entity.downloadedLength)
                if ((needRange && response.code == 206) || (!needRange && response.isSuccessful)) {
                    var delayTime = 0L
                    body = response.body
                    body?.let {
                        val bs = body.byteStream()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var len: Int = bs.read(buffer)
                        while (len != -1) {
                            raf.write(buffer, 0, len)
                            entity.downloadedLength += len
                            if (System.currentTimeMillis() - delayTime > RESPONSE_INTERVAL) {
                                val progress = entity.downloadedLength * 100 / entity.length
                                setProgress(workDataOf(PROGRESS to progress.toInt()))
                                setForeground(createForegroundInfo(progress.toInt()))
                                DatabaseRepo.HanimeDownload.update(entity)
                                delayTime = System.currentTimeMillis()
                            }
                            len = bs.read(buffer)
                        }
                    }
                    showSuccessNotification()
                    return@withContext Result.success()
                } else {
                    Log.d("${TAG}_FailRetry", response.message)
                    return@withContext Result.retry()
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    showFailureNotification(
                        e.message ?: context.getString(R.string.unknown_download_error)
                    )
                    return@withContext Result.failure(workDataOf(FAILED_REASON to e.message))
                }
                // cancellation exception block 是代表用户暂停
                return@withContext Result.success()
            } finally {
                entity.isDownloading = false
                Log.d(TAG, entity.toString())
                DatabaseRepo.HanimeDownload.update(entity)
                raf?.close()
                response?.close()
                body?.close()
            }
        }
    }

    private fun createForegroundInfo(progress: Int = 0): ForegroundInfo {
        return ForegroundInfo(
            downloadId,
            NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setSilent(true)
                .setContentTitle(context.getString(R.string.downloading_s, hanimeName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("$progress%")
                .setProgress(100, progress, false)
                .build(),
            // #issue-34: 這裡的參數是為了讓 Android 14 以上的系統可以正常顯示前景通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
        )
    }

    @SuppressLint("MissingPermission")
    private fun showSuccessNotification() {
        notificationManager.notify(
            successId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_24)
                .setContentTitle(context.getString(R.string.download_task_completed))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(context.getString(R.string.download_completed_s, hanimeName))
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun showFileExistsFailureNotification(fileName: String) {
        notificationManager.notify(
            failId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle(context.getString(R.string.this_data_exists))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(context.getString(R.string.download_failed_s_exists, fileName))
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun showFailureNotification(errMsg: String) {
        notificationManager.notify(
            failId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle(context.getString(R.string.download_task_failed))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(
                    context.getString(
                        R.string.download_task_failed_s_reason_s,
                        hanimeName, errMsg
                    )
                )
                .build()
        )
    }
}