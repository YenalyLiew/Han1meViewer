package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.CommentPlace
import com.yenaly.han1meviewer.logic.model.VideoCommentArgs
import com.yenaly.han1meviewer.logic.model.VideoComments
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.viewmodel.AppViewModel.csrfToken
import com.yenaly.yenaly_libs.base.YenalyViewModel
import com.yenaly.yenaly_libs.utils.showShortToast
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2022/06/28 028 14:18
 */
class CommentViewModel(application: Application) : YenalyViewModel(application) {

    lateinit var code: String

    var currentUserId: String? = null

    private val _videoCommentStateFlow =
        MutableStateFlow<WebsiteState<VideoComments>>(WebsiteState.Loading)
    val videoCommentStateFlow = _videoCommentStateFlow.asStateFlow()

    private val _videoReplyStateFlow =
        MutableStateFlow<WebsiteState<VideoComments>>(WebsiteState.Loading)
    val videoReplyStateFlow = _videoReplyStateFlow.asStateFlow()

    private val _videoCommentFlow = MutableStateFlow(emptyList<VideoComments.VideoComment>())
    val videoCommentFlow = _videoCommentFlow.asStateFlow()

    private val _videoReplyFlow = MutableStateFlow(emptyList<VideoComments.VideoComment>())
    val videoReplyFlow = _videoReplyFlow.asStateFlow()

    private val _postCommentFlow =
        MutableSharedFlow<WebsiteState<Unit>>(replay = 0)
    val postCommentFlow = _postCommentFlow.asSharedFlow()

    private val _postReplyFlow =
        MutableSharedFlow<WebsiteState<Unit>>(replay = 0)
    val postReplyFlow = _postReplyFlow.asSharedFlow()

    private val _commentLikeFlow =
        MutableSharedFlow<WebsiteState<VideoCommentArgs>>(replay = 0)
    val commentLikeFlow = _commentLikeFlow.asSharedFlow()

    fun getComment(type: String, code: String) {
        viewModelScope.launch {
            _videoCommentStateFlow.value = WebsiteState.Loading
            NetworkRepo.getComments(type, code).collect { state ->
                _videoCommentStateFlow.value = state
                _videoCommentFlow.update { prevList ->
                    when (state) {
                        is WebsiteState.Success -> state.info.videoComment
                        is WebsiteState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }

    fun updateComments(comments: List<VideoComments.VideoComment>) {
        _videoCommentFlow.update { comments }
    }

    fun getCommentReply(commentId: String) {
        viewModelScope.launch {
            // 每次获取评论回复时，都会重新加载
            _videoReplyStateFlow.value = WebsiteState.Loading
            NetworkRepo.getCommentReply(commentId).collect { state ->
                _videoReplyStateFlow.value = state
                _videoReplyFlow.update { prevList ->
                    when (state) {
                        is WebsiteState.Success -> state.info.videoComment
                        is WebsiteState.Loading -> emptyList()
                        else -> prevList
                    }
                }
            }
        }
    }

    fun postComment(
        currentUserId: String,
        targetUserId: String,
        type: String,
        text: String,
    ) {
        viewModelScope.launch {
            NetworkRepo.postComment(csrfToken, currentUserId, targetUserId, type, text)
                .collect(_postCommentFlow::emit)
        }
    }

    fun postReply(
        replyCommentId: String,
        text: String,
    ) {
        viewModelScope.launch {
            NetworkRepo.postCommentReply(csrfToken, replyCommentId, text)
                .collect(_postReplyFlow::emit)
        }
    }

    fun likeComment(
        isPositive: Boolean, commentPosition: Int,
        comment: VideoComments.VideoComment, likeCommentStatus: Boolean = false,
        unlikeCommentStatus: Boolean = false,
    ) = likeCommentInternal(
        CommentPlace.COMMENT, isPositive, commentPosition,
        comment, likeCommentStatus, unlikeCommentStatus
    )

    fun likeChildComment(
        isPositive: Boolean, commentPosition: Int,
        comment: VideoComments.VideoComment, likeCommentStatus: Boolean = false,
        unlikeCommentStatus: Boolean = false,
    ) = likeCommentInternal(
        CommentPlace.CHILD_COMMENT, isPositive, commentPosition,
        comment, likeCommentStatus, unlikeCommentStatus
    )

    private fun likeCommentInternal(
        commentPlace: CommentPlace,
        isPositive: Boolean,
        commentPosition: Int,
        comment: VideoComments.VideoComment,
        likeCommentStatus: Boolean = false,
        unlikeCommentStatus: Boolean = false,
    ) {
        viewModelScope.launch {
            NetworkRepo.likeComment(
                csrfToken,
                commentPlace,
                comment.post.foreignId,
                isPositive,
                comment.post.likeUserId,
                comment.post.commentLikesCount ?: 0,
                comment.post.commentLikesSum ?: 0,
                likeCommentStatus,
                unlikeCommentStatus,
                commentPosition, comment
            ).collect { argState ->
                _commentLikeFlow.emit(argState)
                if (argState is WebsiteState.Success) {
                    when (commentPlace) {
                        CommentPlace.COMMENT -> _videoCommentFlow.update { prevList ->
                            prevList.toMutableList().apply {
                                this[commentPosition] =
                                    this[commentPosition].handleCommentLike(argState.info)
                            }
                        }

                        CommentPlace.CHILD_COMMENT -> _videoReplyFlow.update { prevList ->
                            prevList.toMutableList().apply {
                                this[commentPosition] =
                                    this[commentPosition].handleCommentLike(argState.info)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun VideoComments.VideoComment.handleCommentLike(
        args: VideoCommentArgs,
    ) = if (args.isPositive) {
        this.incLikesCount(cancel = post.likeCommentStatus)
    } else {
        this.decLikesCount(cancel = post.unlikeCommentStatus)
    }

    fun handleCommentLike(args: VideoCommentArgs) {
        if (args.isPositive) {
            if (args.comment.post.likeCommentStatus) {
                showShortToast(R.string.cancel_thumb_up_success)
            } else {
                showShortToast(R.string.thumb_up_success)
            }
        } else {
            if (args.comment.post.unlikeCommentStatus) {
                showShortToast(R.string.cancel_thumb_down_success)
            } else {
                showShortToast(R.string.thumb_down_success)
            }
        }
    }
}