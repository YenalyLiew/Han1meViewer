package com.yenaly.han1meviewer.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
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
import com.yenaly.han1meviewer.HFileManager
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.entity.download.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.network.ServiceCreator
import com.yenaly.han1meviewer.logic.state.DownloadState
import com.yenaly.han1meviewer.util.HImageMeower
import com.yenaly.han1meviewer.util.await
import com.yenaly.yenaly_libs.utils.createFileIfNotExists
import com.yenaly.yenaly_libs.utils.saveTo
import com.yenaly.yenaly_libs.utils.showShortToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import java.io.File
import java.io.InputStream
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

    data class Args(
        val quality: String?,
        val downloadUrl: String?,
        val videoType: String?,
        val hanimeName: String,
        val videoCode: String,
        val coverUrl: String,
    ) {
        companion object {
            fun fromEntity(entity: HanimeDownloadEntity): Args {
                return Args(
                    quality = entity.quality,
                    downloadUrl = entity.videoUrl,
                    videoType = entity.suffix,
                    hanimeName = entity.title,
                    videoCode = entity.videoCode,
                    coverUrl = entity.coverUrl,
                )
            }
        }
    }

    companion object {
        const val TAG = "HanimeDownloadWorker"

        const val RESPONSE_INTERVAL = 500L

        const val BACKOFF_DELAY = 10_000L

        const val FAST_PATH_CANCEL = "fast_path_cancel"
        const val DELETE = "delete"
        const val QUALITY = "quality"
        const val DOWNLOAD_URL = "download_url"
        const val VIDEO_TYPE = "video_type"
        const val HANIME_NAME = "hanime_name"
        const val VIDEO_CODE = "video_code"
        const val COVER_URL = "cover_url"
        const val REDOWNLOAD = "redownload"
        const val IN_WAITING_QUEUE = "in_waiting_queue"
        // const val RELEASE_DATE = "release_date"
        // const val COVER_DOWNLOAD = "cover_download"

        const val PROGRESS = "progress"
        // const val FAILED_REASON = "failed_reason"

        /**
         * 方便统一管理下载 Worker 的创建
         */
        inline fun build(
            constraintsRequired: Boolean = true,
            action: OneTimeWorkRequest.Builder.() -> Unit = {}
        ): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build()
            return OneTimeWorkRequestBuilder<HanimeDownloadWorker>()
                .addTag(TAG)
                .let { builder ->
                    if (constraintsRequired) {
                        builder.setConstraints(constraints)
                    } else {
                        builder
                    }
                }.setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    BACKOFF_DELAY, TimeUnit.MILLISECONDS
                ).apply(action).build()
        }

        fun getRunningWorkInfoCount(context: Context): Flow<Int> {
            return WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(TAG)
                .map { workInfos ->
                    workInfos.count {
                        it.state == WorkInfo.State.RUNNING
                    }
                }.distinctUntilChanged()
        }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    private val hanimeName by inputData(HANIME_NAME, EMPTY_STRING)
    private val downloadUrl by inputData(DOWNLOAD_URL, EMPTY_STRING)
    private val videoType by inputData(VIDEO_TYPE, HFileManager.DEF_VIDEO_TYPE)
    private val quality by inputData(QUALITY, EMPTY_STRING)
    private val videoCode by inputData(VIDEO_CODE, EMPTY_STRING)
    private val coverUrl by inputData(COVER_URL, EMPTY_STRING)

    private val fastPathCancel by inputData(FAST_PATH_CANCEL, false)
    private val shouldDelete by inputData(DELETE, false)
    private val shouldRedownload by inputData(REDOWNLOAD, false)
    private val isInWaitingQueue by inputData(IN_WAITING_QUEUE, false)

    private val downloadId = Random.nextInt()

    private val mainScope = CoroutineScope(Dispatchers.Main.immediate)
    private val dbScope = CoroutineScope(Dispatchers.IO)

    override suspend fun doWork(): Result {
        if (fastPathCancel) return Result.success()
        setForeground(createForegroundInfo())
        return download()
    }

    private suspend fun createNewRaf(file: File) {
        return withContext(Dispatchers.IO) {
            var raf: RandomAccessFile? = null
            var response: Response? = null
            var body: ResponseBody? = null
            try {
                file.createFileIfNotExists()
                raf = RandomAccessFile(file, "rwd")
                val request = Request.Builder().url(downloadUrl).get().build()
                response = ServiceCreator.downloadClient.newCall(request).await()
                if (response.isSuccessful) {
                    body = response.body
                    body?.let {
                        val len = body.contentLength()
                        if (len > 0) {
                            raf.setLength(len)
                            val entity = HanimeDownloadEntity(
                                // 创建文件时不需要下载 coverImage
                                coverUrl = coverUrl, coverUri = null,
                                title = hanimeName,
                                addDate = System.currentTimeMillis(), videoCode = videoCode,
                                videoUri = file.toUri().toString(), quality = quality,
                                videoUrl = downloadUrl, length = len, downloadedLength = 0,
                                // isDownloading = false
                                state = DownloadState.Queued
                            )
                            DatabaseRepo.HanimeDownload.insert(entity)
                        }
                    }
                }
            } catch (e: Exception) {
                // 创建，但是并没有下载接收到文件大小，删除文件
                if (file.exists() && file.length() == 0L) {
                    // HFileManager.getDownloadVideoFolder(videoCode).deleteRecursively()
                    // 不应该直接删除文件夹，因为可能存在其他分辨率的文件
                    dbScope.launch {
//                        val count = DatabaseRepo.HanimeDownload.countBy(videoCode)
//                        HFileManager.deleteDownload(videoCode, count, file)
                        HFileManager.getDownloadVideoFolder(videoCode).deleteRecursively()
                    }
                }
                e.printStackTrace()
            } finally {
                raf?.closeQuietly()
                response?.closeQuietly()
                body?.closeQuietly()
            }
        }
    }

    private suspend fun download(): Result {
        return withContext(Dispatchers.IO) {
            val file = HFileManager.getDownloadVideoFile(
                videoCode, hanimeName, quality, suffix = videoType
            )
            // redownload 不一定要删除全部文件夹，因为可能有不同分辨率
            if (shouldRedownload || shouldDelete) {
                // 注意顺序
//                val count = DatabaseRepo.HanimeDownload.countBy(videoCode)
//                HFileManager.deleteDownload(videoCode, count, file)
                HFileManager.getDownloadVideoFolder(videoCode).deleteRecursively()
                DatabaseRepo.HanimeDownload.delete(videoCode)
                if (shouldDelete) {
                    return@withContext Result.success()
                }
            }
            val entity = DatabaseRepo.HanimeDownload.find(videoCode, quality) ?: kotlin.run {
                // 如果不存在，创建新的 raf
                createNewRaf(file)
                DatabaseRepo.HanimeDownload.find(videoCode, quality)
                    ?: return@withContext kotlin.run {
                        Log.d(TAG, "entity is null, create new raf failed")
                        showFailureNotification(context.getString(R.string.get_file_info_failed))
                        mainScope.launch {
                            showShortToast(
                                context.getString(R.string.download_task_failed_s, hanimeName)
                            )
                        }
                        Result.failure()
                    }
            }

            if (entity.coverUri == null) {
                updateCoverImage(entity)
            }
            if (isInWaitingQueue) {
                DatabaseRepo.HanimeDownload.update(
                    entity.copy(state = DownloadState.Queued)
                )
                return@withContext Result.success()
            }

            var downloadedLength = entity.downloadedLength
            val needRange = entity.downloadedLength > 0
            var raf: RandomAccessFile? = null
            var response: Response? = null
            var body: ResponseBody? = null
            var bodyStream: InputStream? = null

            var result: Result = Result.failure()
            try {
                raf = RandomAccessFile(file, "rwd")
                val request = Request.Builder().url(downloadUrl)
                    .also { if (needRange) it.header("Range", "bytes=${entity.downloadedLength}-") }
                    .get().build()
                response = ServiceCreator.downloadClient.newCall(request).await()
                raf.seek(entity.downloadedLength)
                if ((needRange && response.code == 206) || (!needRange && response.isSuccessful)) {
                    var delayTime = 0L
                    body = response.body
                    if (body != null) {
                        bodyStream = body.byteStream()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var len: Int = bodyStream.read(buffer)
                        while (len != -1) {
                            raf.write(buffer, 0, len)
                            downloadedLength += len
                            if (System.currentTimeMillis() - delayTime > RESPONSE_INTERVAL) {
                                val progress = downloadedLength * 100 / entity.length
                                setProgress(workDataOf(PROGRESS to progress.toInt()))
                                updateDownloadNotification(progress.toInt())
                                DatabaseRepo.HanimeDownload.update(
                                    entity.copy(
                                        downloadedLength = downloadedLength,
                                        // isDownloading = true,
                                        state = DownloadState.Downloading
                                    )
                                )
                                delayTime = System.currentTimeMillis()
                            }
                            len = bodyStream.read(buffer)
                        }
                    }
                    showSuccessNotification()
                    result = Result.success(
                        workDataOf(DownloadState.STATE to DownloadState.Finished.mask)
                    )
                } else {
                    Log.d(TAG, "response failed: ${response.message}")
                    showFailureNotification(response.message)
                    mainScope.launch {
                        showShortToast(
                            context.getString(R.string.download_task_failed_s, hanimeName)
                        )
                    }
                    result = Result.failure(
                        workDataOf(DownloadState.STATE to DownloadState.Failed.mask)
                    )
                }
            } catch (e: Exception) {
                result = if (e is CancellationException) {
                    // cancellation exception block 是代表用户暂停
                    cancelDownloadNotification()
                    Result.success(
                        workDataOf(DownloadState.STATE to DownloadState.Paused.mask)
                    )
                } else {
                    showFailureNotification(e.localizedMessage)
                    e.printStackTrace()
                    mainScope.launch {
                        showShortToast(e.localizedMessage)
                    }
                    Result.failure(
                        workDataOf(DownloadState.STATE to DownloadState.Failed.mask)
                    )
                }
            } finally {
                // HanimeDownloadManager.notify(newEntity)

                val state = DownloadState.from(
                    result.outputData.getInt(DownloadState.STATE, DownloadState.Unknown.mask)
                )
                // 为什么要用 dbScope 包住？
                // 使用 dbScope 是为了确保即使当前协程因任务取消而失效，
                // “update”挂起函数仍然能够找到有效的协程作用域来更新数据库。
                // 这也是一个历史遗留问题。
                dbScope.launch {
                    val newEntity = entity.copy(
                        // isDownloading = false,
                        state = state,
                        downloadedLength = downloadedLength
                    )
                    DatabaseRepo.HanimeDownload.update(newEntity)
                    Log.d(TAG, "finally -> $newEntity")
                }
                raf?.closeQuietly()
                response?.closeQuietly()
                body?.closeQuietly()
                bodyStream?.closeQuietly()
            }
            return@withContext result
        }
    }

    private fun CoroutineScope.updateCoverImage(entity: HanimeDownloadEntity) {
        launch {
            val imgRes = HImageMeower.execute(entity.coverUrl)
            val file = HFileManager.getDownloadVideoCoverFile(videoCode, hanimeName)
            val isSuccess = imgRes.drawable?.saveTo(file) == true
            if (isSuccess) {
                val coverUri = file.toUri().toString()
                DatabaseRepo.HanimeDownload.update(entity.copy(coverUri = coverUri))
                // 不得不用 var，要不然有点难搞
                entity.coverUri = coverUri
            }
        }
    }

    private fun createDownloadNotification(progress: Int = 0): Notification {
        return NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(context.getString(R.string.downloading_s, hanimeName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText("$progress%")
            .setProgress(100, progress, false)
            .build()
    }

    private fun cancelDownloadNotification() {
        notificationManager.cancel(downloadId)
    }

    @SuppressLint("MissingPermission")
    private fun updateDownloadNotification(progress: Int) {
        notificationManager.notify(downloadId, createDownloadNotification(progress))
    }

    private fun createForegroundInfo(progress: Int = 0): ForegroundInfo {
        val notification = createDownloadNotification(progress)
        return ForegroundInfo(
            downloadId, notification,
            // #issue-34: 這裡的參數是為了讓 Android 14 以上的系統可以正常顯示前景通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else 0
        )
    }

    @SuppressLint("MissingPermission")
    private fun showSuccessNotification() {
        notificationManager.notify(
            downloadId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_24)
                .setContentTitle(context.getString(R.string.download_task_completed))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.download_completed_s, hanimeName))
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun showFileExistsFailureNotification(fileName: String) {
        notificationManager.notify(
            downloadId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle(context.getString(R.string.this_data_exists))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(context.getString(R.string.download_failed_s_exists, fileName))
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun showFailureNotification(errMsg: String? = null) {
        notificationManager.notify(
            downloadId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle(context.getString(R.string.download_task_failed))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentText(
                    context.getString(
                        R.string.download_task_failed_s_reason_s,
                        hanimeName, errMsg ?: context.getString(R.string.unknown_download_error)
                    )
                )
                .build()
        )
    }
}