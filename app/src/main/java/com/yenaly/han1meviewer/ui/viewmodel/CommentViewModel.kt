package com.yenaly.han1meviewer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.logic.NetworkRepo
import com.yenaly.han1meviewer.logic.model.CommentPlace
import com.yenaly.han1meviewer.logic.model.VideoCommentArguments
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
import com.yenaly.han1meviewer.logic.state.WebsiteState
import com.yenaly.han1meviewer.ui.adapter.VideoCommentRvAdapter
import com.yenaly.yenaly_libs.base.YenalyViewModel
import com.yenaly.yenaly_libs.utils.showShortToast
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

    private val _commentLikeFlow =
        MutableSharedFlow<WebsiteState<VideoCommentArguments>>(replay = 0)
    val commentLikeFlow = _commentLikeFlow.asSharedFlow()

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
        comment: VideoCommentModel.VideoComment, likeCommentStatus: Boolean = false,
        unlikeCommentStatus: Boolean = false,
    ) = likeCommentInternal(
        CommentPlace.COMMENT, isPositive, commentPosition,
        comment, likeCommentStatus, unlikeCommentStatus
    )

    fun likeChildComment(
        isPositive: Boolean, commentPosition: Int,
        comment: VideoCommentModel.VideoComment, likeCommentStatus: Boolean = false,
        unlikeCommentStatus: Boolean = false,
    ) = likeCommentInternal(
        CommentPlace.CHILD_COMMENT, isPositive, commentPosition,
        comment, likeCommentStatus, unlikeCommentStatus
    )

    private fun likeCommentInternal(
        commentPlace: CommentPlace,
        isPositive: Boolean,
        commentPosition: Int,
        comment: VideoCommentModel.VideoComment,
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
            ).collect(_commentLikeFlow::emit)
        }
    }

    fun handleCommentLike(args: VideoCommentArguments, adapter: VideoCommentRvAdapter) {
        if (args.isPositive) {
            if (args.comment.post.likeCommentStatus) {
                showShortToast(R.string.cancel_thumb_up_success)
                args.comment.incrementLikesCount(cancel = true)
                adapter.notifyItemChanged(args.commentPosition)
            } else {
                showShortToast(R.string.thumb_up_success)
                args.comment.incrementLikesCount(cancel = false)
                adapter.notifyItemChanged(args.commentPosition)
            }
        } else {
            if (args.comment.post.unlikeCommentStatus) {
                showShortToast(R.string.cancel_thumb_down_success)
                args.comment.decrementLikesCount(cancel = true)
                adapter.notifyItemChanged(args.commentPosition)
            } else {
                showShortToast(R.string.thumb_down_success)
                args.comment.decrementLikesCount(cancel = false)
                adapter.notifyItemChanged(args.commentPosition)
            }
        }
    }
}