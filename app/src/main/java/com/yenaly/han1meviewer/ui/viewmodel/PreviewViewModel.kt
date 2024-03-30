package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.HanimePreview
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/23 023 16:47
 */
class PreviewViewModel(application: Application) : YenalyViewModel(application) {

    private val _previewFlow =
        MutableStateFlow<WebsiteState<HanimePreview>>(WebsiteState.Loading)
    val previewFlow = _previewFlow.asStateFlow()

    fun getHanimePreview(date: String) {
        viewModelScope.launch {
            NetworkRepo.getHanimePreview(date).collect { preview ->
                _previewFlow.value = preview
            }
        }
    }
}