package com.yenaly.han1meviewer.service

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.*
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadedEntity
import com.yenaly.han1meviewer.logic.network.ServiceCreator
import com.yenaly.han1meviewer.notificationManager
import com.yenaly.yenaly_libs.utils.copyTo
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import kotlin.random.Random

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 11:42
 */
class HanimeDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "hanime_download_worker"

        const val QUALITY = "quality"
        const val DOWNLOAD_URL = "download_url"
        const val HANIME_NAME = "hanime_name"
        const val VIDEO_CODE = "video_code"
        const val COVER_URL = "cover_url"
        const val RELEASE_DATE = "release_date"
        // const val COVER_DOWNLOAD = "cover_download"

        const val PROGRESS = "progress"
    }

    private val hanimeName by unsafeLazy {
        checkNotNull(inputData.getString(HANIME_NAME))
    }
    private val quality by unsafeLazy {
        checkNotNull(inputData.getString(QUALITY))
    }
    private val downloadUrl by unsafeLazy {
        checkNotNull(inputData.getString(DOWNLOAD_URL))
    }
    private val videoCode by unsafeLazy {
        checkNotNull(inputData.getString(VIDEO_CODE))
    }
    private val coverUrl by unsafeLazy {
        checkNotNull(inputData.getString(COVER_URL))
    }
    private val releaseDate by unsafeLazy {
        checkNotNull(inputData.getLong(RELEASE_DATE, 0))
    }

    private val downloadId = Random.nextInt()
    private val successId = Random.nextInt()
    private val failId = Random.nextInt()

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())
        return withContext(Dispatchers.IO) {
            downloadHanime(downloadUrl, hanimeName, quality)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadHanime(url: String, name: String, quality: String): Result {
        val file = getDownloadedHanimeFile(name, quality)
        val request = Request.Builder().url(url).get().build()
        val response = ServiceCreator.okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            response.body!!.let { responseBody ->
                val total = responseBody.contentLength()
                file.outputStream().use { output ->
                    var emittedProgress = 0L
                    responseBody.byteStream().use { input ->
                        input.copyTo(output) { bytesCopied ->
                            val progress = bytesCopied * 100 / total
                            if (progress - emittedProgress >= 5) {
                                setProgressAsync(workDataOf(PROGRESS to progress.toInt()))
                                Log.d("progress", progress.toInt().toString())
                                setForegroundAsync(createForegroundInfo(progress.toInt()))
                                emittedProgress = progress
                            }
                        }
                    }
                }
                // 存在該videoCode的影片記錄走這裏，
                // 更新一下直接返回
                DatabaseRepo.loadDownloadedHanimeByVideoCode(videoCode)?.let { data ->
                    // old
                    data.videoUri.toUri().toFile().delete()
                    // new
                    data.quality = quality
                    data.videoUri = file.toUri().toString()
                    data.addDate = System.currentTimeMillis()
                    // update
                    DatabaseRepo.updateDownloadedHanime(data)

                    showSuccessNotification()
                    return Result.success()
                }

                // 若爲空走這裏，直接插入，最後返回
                DatabaseRepo.insertDownloadedHanime(
                    HanimeDownloadedEntity(
                        coverUrl = coverUrl, title = hanimeName, releaseDate = releaseDate,
                        addDate = System.currentTimeMillis(), videoCode = videoCode,
                        videoUri = file.toUri().toString(), quality = quality
                    )
                )

                showSuccessNotification()
                return Result.success()
            }
        } else {
            showFailureNotification(response.message)
            if (file.exists()) file.delete()
            return Result.retry()
        }
    }

    private fun createForegroundInfo(progress: Int = 0): ForegroundInfo {
        return ForegroundInfo(
            downloadId,
            NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("正在下載：${hanimeName}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("$progress%")
                .setProgress(100, progress, false)
                .build()
        )
    }

    private fun showSuccessNotification() {
        notificationManager.notify(
            successId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_check_circle_24)
                .setContentTitle("下載任務已完成！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("下載完畢：${hanimeName}")
                .build()
        )
    }

    private fun showFileExistsFailureNotification(fileName: String) {
        notificationManager.notify(
            failId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle("該檔案已存在！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("下載失敗：${fileName} 已存在")
                .build()
        )
    }

    private fun showFailureNotification(errMsg: String) {
        notificationManager.notify(
            failId, NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setContentTitle("下載任務失敗！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText("下載失敗：${hanimeName}\n原因為：${errMsg}")
                .build()
        )
    }
}