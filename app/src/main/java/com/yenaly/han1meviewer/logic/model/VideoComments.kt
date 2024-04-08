package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/20 020 21:56
 */
data class VideoComments(
    val videoComment: MutableList<VideoComment>,
    val currentUserId: String? = null,
    val csrfToken: String? = null,
) {
    data class VideoComment(
        // 頭像
        val avatar: String,
        // 作者
        val username: String,
        // 發佈日期
        val date: String,
        // 内容
        val content: String,
        // 點讚數量，登入前不能憑藉post獲取
        var thumbUp: Int? = null,
        // 是否为子评论
        val isChildComment: Boolean,
        // 是否有更多回覆
        val hasMoreReplies: Boolean = false,
        // 評論id，登入前不能憑藉post獲取
        val id: String? = null,
        // post 相關
        val post: POST,
    ) {

        val realReplyId get() = post.foreignId ?: checkNotNull(id)
        val realLikesCount get() = thumbUp
        fun incLikesCount(cancel: Boolean = false): VideoComment {
            return thumbUp?.let {
                copy(
                    thumbUp = it + if (cancel) -1 else 1,
                    post = post.copy(
                        likeCommentStatus = !cancel,
                        unlikeCommentStatus = false
                    )
                )
            } ?: this
        }

        fun decLikesCount(cancel: Boolean = false): VideoComment {
            return thumbUp?.let {
                copy(
                    thumbUp = it - if (cancel) -1 else 1,
                    post = post.copy(
                        likeCommentStatus = false,
                        unlikeCommentStatus = !cancel
                    )
                )
            } ?: this
        }

        data class POST(
            // 對方id
            val foreignId: String? = null,
            // 你點的
            var isPositive: Boolean = false,
            // 你的id
            val likeUserId: String? = null,
            var commentLikesCount: Int? = null,
            var commentLikesSum: Int? = null,
            // 你之前有沒有點過讚
            var likeCommentStatus: Boolean = false,
            // 你之前有沒有點過踩
            var unlikeCommentStatus: Boolean = false,
        )
    }
}

/**
 * 用於 評論交互 Flow 的返回值
 */
data class VideoCommentArgs(
    // 當前評論所處adapter位置
    val commentPosition: Int,
    // 你當前點擊的是讚還是踩，和comment裏的isPositive不一樣
    val isPositive: Boolean,
    val comment: VideoComments.VideoComment,
)