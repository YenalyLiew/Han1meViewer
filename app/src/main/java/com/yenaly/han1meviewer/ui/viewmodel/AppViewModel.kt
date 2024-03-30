package com.yenaly.han1meviewer.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.worker.HUpdateWorker
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.utils.applicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/29 029 18:00
 */
@SuppressLint("StaticFieldLeak")
object AppViewModel {

    /**
     * App CoroutineScope
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val context: Context = applicationContext

    private val _versionFlow = MutableStateFlow<WebsiteState<Latest?>>(WebsiteState.Loading)
    val versionFlow = _versionFlow.asStateFlow()

    init {
        // 取消，防止每次启动都有残留的更新任务
        WorkManager.getInstance(context).pruneWork()

        appScope.launch(Dispatchers.Main) {
            HUpdateWorker.collectOutput(context)
        }

        appScope.launch(Dispatchers.Main) {
            HanimeDownloadWorker.collectOutput(context)
        }

        appScope.launch {
            // 不要太提前
            delay(500)
            getLatestVersionSuspend()
        }
    }

    fun getLatestVersion(forceCheck: Boolean = true) {
        appScope.launch {
            getLatestVersionSuspend(forceCheck)
        }
    }

    private suspend fun getLatestVersionSuspend(forceCheck: Boolean = true) {
        NetworkRepo.getLatestVersion(forceCheck).collect {
            _versionFlow.value = it
        }
    }
}