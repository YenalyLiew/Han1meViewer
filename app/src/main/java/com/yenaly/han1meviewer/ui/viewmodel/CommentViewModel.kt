package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.yenaly_libs.base.YenalyViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/06/28 028 14:18
 */
class CommentViewModel(application: Application) : YenalyViewModel(application) {

    lateinit var code: String

    var csrfToken: String? = null
    var currentUserId: String? = null

    private val _videoCommentFlow =
        MutableStateFlow<WebsiteState<VideoCommentModel>>(WebsiteState.Loading)
    val videoCommentFlow = _videoCommentFlow.asStateFlow()

    private val _videoReplyFlow =
        MutableStateFlow<WebsiteState<VideoCommentModel>>(WebsiteState.Loading)
    val videoReplyFlow = _videoReplyFlow.asStateFlow()

    private val _postCommentFlow =
        MutableSharedFlow<WebsiteState<Unit>>(replay = 0)
    val postCommentFlow = _postCommentFlow.asSharedFlow()

    private val _postReplyFlow =
        MutableSharedFlow<WebsiteState<Unit>>(replay = 0)
    val postReplyFlow = _postReplyFlow.asSharedFlow()

    fun getComment(type: String, code: String) {
        viewModelScope.launch {
            NetworkRepo.getComments(type, code).collect { comment ->
                _videoCommentFlow.value = comment
            }
        }
    }

    fun getCommentReply(commentId: String) {
        viewModelScope.launch {
            NetworkRepo.getCommentReply(commentId).collect { reply ->
                _videoReplyFlow.value = reply
            }
        }
    }

    fun postComment(
        csrfToken: String?,
        currentUserId: String,
        targetUserId: String,
        type: String,
        text: String
    ) {
        viewModelScope.launch {
            NetworkRepo.postComment(csrfToken, currentUserId, targetUserId, type, text)
                .collect(_postCommentFlow::emit)
        }
    }

    fun postReply(
        csrfToken: String?,
        replyCommentId: String,
        text: String
    ) {
        viewModelScope.launch {
            NetworkRepo.postCommentReply(csrfToken, replyCommentId, text)
                .collect(_postReplyFlow::emit)
        }
    }
}