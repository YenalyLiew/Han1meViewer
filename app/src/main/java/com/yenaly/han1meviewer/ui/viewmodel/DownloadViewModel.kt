package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/08/02 002 12:05
 */
class DownloadViewModel(application: Application) : YenalyViewModel(application) {

    fun loadAllDownloadedHanime() =
        DatabaseRepo.loadAllDownloadedHanime()
            .catch { e -> e.printStackTrace() }
            .flowOn(Dispatchers.IO)

    fun deleteDownloadedHanimeByVideoCode(videoCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.deleteDownloadedHanimeByVideoCode(videoCode)
        }
    }
}