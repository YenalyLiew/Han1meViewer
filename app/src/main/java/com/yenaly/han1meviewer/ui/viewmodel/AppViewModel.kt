package com.yenaly.han1meviewer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.setCustomKeys
import com.yenaly.han1meviewer.FirebaseConstants
import com.yenaly.han1meviewer.Preferences
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.github.Latest
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.worker.HUpdateWorker
import com.yenaly.han1meviewer.worker.HanimeDownloadWorker
import com.yenaly.yenaly_libs.base.YenalyViewModel
import com.yenaly.yenaly_libs.utils.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/03/29 029 18:00
 */
object AppViewModel : YenalyViewModel(application), IHCsrfToken {

    /**
     * csrfToken 全局唯一，只需要在首页拉起或点击视频页时更新一下就可以了
     */
    override var csrfToken: String? = null

    private val _versionFlow = MutableStateFlow<WebsiteState<Latest?>>(WebsiteState.Loading)
    val versionFlow = _versionFlow.asStateFlow()

    init {
        // 取消，防止每次启动都有残留的更新任务
        WorkManager.getInstance(application).pruneWork()

        viewModelScope.launch {
            Preferences.loginStateFlow.collect { isLogin ->
                Log.d("LoginState", "isLogin: $isLogin")
                Firebase.crashlytics.setCustomKeys {
                    key(FirebaseConstants.LOGIN_STATE, isLogin)
                }
            }
        }

        viewModelScope.launch(Dispatchers.Main) {
            HUpdateWorker.collectOutput(application)
        }

        viewModelScope.launch(Dispatchers.Main) {
            HanimeDownloadWorker.collectOutput(application)
        }
    }

    fun getLatestVersion(forceCheck: Boolean = true, delayMillis: Long = 0) {
        viewModelScope.launch {
            delay(delayMillis)
            getLatestVersionSuspend(forceCheck)
        }
    }

    private suspend fun getLatestVersionSuspend(forceCheck: Boolean = true) {
        NetworkRepo.getLatestVersion(forceCheck).collect {
            _versionFlow.value = it
        }
    }
}