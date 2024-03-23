package com.yenaly.han1meviewer

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.google.android.material.color.DynamicColors
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.yenaly.han1meviewer.logic.network.HProxySelector
import com.yenaly.han1meviewer.worker.HUpdateWorker
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.base.YenalyApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:32
 */
class HanimeApplication : YenalyApplication() {

    companion object {
        const val TAG = "HanimeApplication"

        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                return@setDefaultRefreshHeaderCreator MaterialHeader(context)
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                return@setDefaultRefreshFooterCreator ClassicsFooter(context)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        HProxySelector.rebuildNetwork()

        initNotificationChannel()
        initWorkManager()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun initWorkManager() {
        // 取消，防止每次启动都有残留的更新任务
        WorkManager.getInstance(this).pruneWork()

        GlobalScope.launch(Dispatchers.Main) {
            HUpdateWorker.collectOutput(this@HanimeApplication)
        }

        GlobalScope.launch(Dispatchers.Main) {
            HanimeDownloadWorker.collectOutput(this@HanimeApplication)
        }
    }

    private fun initNotificationChannel() {
        val nm = NotificationManagerCompat.from(this)

        val hanimeDownloadChannel = NotificationChannelCompat.Builder(
            DOWNLOAD_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH
        ).setName("Hanime Download").build()
        nm.createNotificationChannel(hanimeDownloadChannel)

        val appUpdateChannel = NotificationChannelCompat.Builder(
            UPDATE_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH
        ).setName("App Update").build()
        nm.createNotificationChannel(appUpdateChannel)
    }
}