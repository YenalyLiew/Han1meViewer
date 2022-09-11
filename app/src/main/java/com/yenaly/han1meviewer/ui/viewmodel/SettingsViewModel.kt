package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.VersionModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 13:40
 */
class SettingsViewModel(application: Application) : YenalyViewModel(application),
    IVersionViewModel {

    private val _versionFlow =
        MutableStateFlow<WebsiteState<VersionModel>>(WebsiteState.Loading())
    val versionFlow = _versionFlow.asStateFlow()

    override fun getLatestVersion() {
        viewModelScope.launch {
            NetworkRepo.getLatestVersion().collect {
                _versionFlow.value = it
            }
        }
    }

    init {
        viewModelScope.launch {
            NetworkRepo.getLatestVersion().collect {
                _versionFlow.value = it
            }
        }
    }
}