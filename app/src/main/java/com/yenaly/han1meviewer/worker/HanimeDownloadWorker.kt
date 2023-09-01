package com.yenaly.han1meviewer.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.*
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.entity.HanimeDownloadEntity
import com.yenaly.han1meviewer.logic.network.ServiceCreator
import com.yenaly.han1meviewer.util.getDownloadedHanimeFile
import com.yenaly.yenaly_libs.utils.unsafeLazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.RandomAccessFile
import java.util.concurrent.CancellationException
import kotlin.random.Random

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/06 006 11:42
 */
class HanimeDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "hanime_download_worker"

        const val RESPONSE_INTERVAL = 500L

        const val QUALITY = "quality"
        const val DOWNLOAD_URL = "download_url"
        const val HANIME_NAME = "hanime_name"
        const val VIDEO_CODE = "video_code"
        const val COVER_URL = "cover_url"
        // const val RELEASE_DATE = "release_date"
        // const val COVER_DOWNLOAD = "cover_download"

        const val PROGRESS = "progress"
        const val FAILED_REASON = "failed_reason"
    }

    private val hanimeName by inputData(HANIME_NAME, EMPTY_STRING)
    private val downloadUrl by inputData(DOWNLOAD_URL, EMPTY_STRING)
    private val quality by inputData(QUALITY, EMPTY_STRING)
    private val videoCode by inputData(VIDEO_CODE, EMPTY_STRING)
    private val coverUrl by inputData(COVER_URL, EMPTY_STRING)

    private val downloadId = Random.nextInt()
    private val successId = Random.nextInt()
    private val failId = Random.nextInt()

    override suspend fun doWork(): Result {
        if (runAttemptCount > 2) {
            return Result.failure(workDataOf(FAILED_REASON to "下載失敗三回啊三回！"))
        }
        setForeground(createForegroundInfo())
        return download()
    }

    private suspend fun createNewRaf() {
        return withContext(Dispatchers.IO) {
            val file = getDownloadedHanimeFile(hanimeName, quality)
            var raf: RandomAccessFile? = null
            var response: Response? = null
            var body: ResponseBody? = null
            try {
                raf = RandomAccessFile(file, "rwd")
                val request = Request.Builder().url(downloadUrl).get().build()
                response = ServiceCreator.okHttpClient.newCall(request).execute()
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
            } catch (e: Exception) {
                // 创建，但是并没有下载接收到文件大小，删除文件
                if (file.length() == 0L) file.delete()
            } finally {
                raf?.close()
                response?.close()
                body?.close()
            }
        }
    }

    private suspend fun download(): Result {
        return withContext(Dispatchers.IO) {
            val file = getDownloadedHanimeFile(hanimeName, quality)
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
                response = ServiceCreator.okHttpClient.newCall(request).execute()
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
                    showFailureNotification(response.message)
                    return@withContext Result.failure(workDataOf(FAILED_REASON to response.message))
                }
            } catch (e: Exception) {
                // cancellation exception block 是代表用户暂停
                if (e !is CancellationException) {
                    showFailureNotification(e.message ?: "未知下載錯誤")
                    return@withContext Result.failure(workDataOf(FAILED_REASON to e.message))
                }
                return@withContext Result.success()
            } finally {
                raf?.close()
                response?.close()
                body?.close()
                entity.isDownloading = false
                Log.d(TAG, entity.toString())
                DatabaseRepo.HanimeDownload.update(entity)
            }
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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

    @Suppress("UNCHECKED_CAST", "SameParameterValue")
    private fun <T : Any> inputData(key: String, def: T): Lazy<T> = unsafeLazy {
        when (def) {
            is String -> (inputData.getString(key) ?: def) as T
            is Int -> inputData.getInt(key, def) as T
            is Long -> inputData.getLong(key, def) as T
            is Boolean -> inputData.getBoolean(key, def) as T
            is Float -> inputData.getFloat(key, def) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}