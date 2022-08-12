package com.yenaly.han1meviewer

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.google.android.material.color.DynamicColors
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.yenaly.yenaly_libs.base.YenalyApplication

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/08 008 17:32
 */
class HanimeApplication : YenalyApplication() {

    companion object {
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

        val channel = NotificationChannelCompat.Builder(
            DOWNLOAD_NOTIFICATION_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_HIGH
        ).setName("Hanime Download").build()
        notificationManager.createNotificationChannel(channel)
    }
}