package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/06/28 028 14:18
 */
class CommentViewModel(application: Application) : YenalyViewModel(application) {

    lateinit var code: String

    private val _videoCommentFlow =
        MutableStateFlow<WebsiteState<MutableList<VideoCommentModel>>>(WebsiteState.Loading())
    val videoCommentFlow = _videoCommentFlow.asStateFlow()

    fun getComment(type: String, code: String) {
        viewModelScope.launch {
            NetworkRepo.getHanimeVideoComment(type, code).collect { comment ->
                _videoCommentFlow.value = comment
            }
        }
    }
}