package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.DatabaseRepo
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.logic.model.VersionModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/07/01 001 13:40
 */
class SettingsViewModel(application: Application) : YenalyViewModel(application),
    IVersionViewModel {

    private val _versionFlow =
        MutableStateFlow<WebsiteState<VersionModel>>(WebsiteState.Loading)
    val versionFlow = _versionFlow.asStateFlow()

    override fun getLatestVersion() {
        viewModelScope.launch {
            NetworkRepo.getLatestVersion().collect {
                _versionFlow.value = it
            }
        }
    }

    fun loadAllHKeyframes(keyword: String? = null) =
        DatabaseRepo.HKeyframe.loadAll(keyword).flowOn(Dispatchers.IO)

    fun removeHKeyframe(videoCode: String, keyframe: HKeyframeEntity.Keyframe) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.removeKeyframe(videoCode, keyframe)
        }
    }


    fun modifyHKeyframe(
        videoCode: String,
        oldKeyframe: HKeyframeEntity.Keyframe, keyframe: HKeyframeEntity.Keyframe,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.modifyKeyframe(videoCode, oldKeyframe, keyframe)
        }
    }

    fun insertHKeyframes(entity: HKeyframeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.insert(entity)
        }
    }

    fun deleteHKeyframes(entity: HKeyframeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.delete(entity)
        }
    }

    fun updateHKeyframes(entity: HKeyframeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            DatabaseRepo.HKeyframe.update(entity)
        }
    }

    fun loadAllSharedHKeyframes() =
        DatabaseRepo.HKeyframe.loadAllShared().flowOn(Dispatchers.IO)

    init {
        getLatestVersion()
    }
}