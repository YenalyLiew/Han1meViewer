package com.yenaly.han1meviewer.logic.model

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/20 020 21:56
 */
data class VideoCommentModel(
    val videoComment: MutableList<VideoComment>,
    val currentUserId: String? = null,
    val csrfToken: String? = null
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
        // 點讚數量
        val thumbUp: String,
        // 是否为子评论
        val isChildComment: Boolean,
        // token
        val csrfToken: String? = null,
        // 是否有更多回覆
        val hasMoreReplies: Boolean = false,
        // comment代號
        val id: String? = null
    )
}