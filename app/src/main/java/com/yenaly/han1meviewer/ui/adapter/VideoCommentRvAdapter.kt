package com.yenaly.han1meviewer.ui.adapter

import android.graphics.Typeface
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.itxca.spannablex.spannable
import com.lxj.xpopup.XPopup
import com.yenaly.han1meviewer.COMMENT_ID
import com.yenaly.han1meviewer.CSRF_TOKEN
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemVideoCommentBinding
import com.yenaly.han1meviewer.isAlreadyLogin
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
import com.yenaly.han1meviewer.ui.fragment.CommentFragment
import com.yenaly.han1meviewer.ui.fragment.CommentReplyFragment
import com.yenaly.han1meviewer.ui.popup.CommentPopup
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.showShortToast

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/21 021 00:03
 */
class VideoCommentRvAdapter constructor() :
    BaseQuickAdapter<VideoCommentModel.VideoComment, VideoCommentRvAdapter.ViewHolder>(R.layout.item_video_comment) {

    private var commentFragment: CommentFragment? = null
    private var replyFragment: CommentReplyFragment? = null
    var commentPopup: CommentPopup? = null

    constructor(commentFragment: CommentFragment) : this() {
        this.commentFragment = commentFragment
    }

    constructor(replyFragment: CommentReplyFragment) : this() {
        this.replyFragment = replyFragment
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<VideoCommentModel.VideoComment>() {
            override fun areItemsTheSame(
                oldItem: VideoCommentModel.VideoComment,
                newItem: VideoCommentModel.VideoComment
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: VideoCommentModel.VideoComment,
                newItem: VideoCommentModel.VideoComment
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemVideoCommentBinding>(view) {
        val binding = dataBinding!!
    }

    override fun convert(holder: ViewHolder, item: VideoCommentModel.VideoComment) {
        holder.binding.ivAvatar.load(item.avatar) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        holder.binding.tvContent.text = item.content
        holder.binding.tvDate.text = item.date
        holder.binding.tvUsername.text = item.username
        holder.binding.btnViewMoreReplies.isVisible = item.hasMoreReplies
        holder.binding.btnThumbUp.text = item.thumbUp
        Log.d("comment_id", item.id.toString())
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.binding.btnViewMoreReplies.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            item.id?.let { id ->
                (CommentReplyFragment().makeBundle(
                    COMMENT_ID to id,
                    CSRF_TOKEN to commentFragment?.viewModel?.csrfToken
                ) as CommentReplyFragment).showIn(context as FragmentActivity)
            }
        }
        viewHolder.binding.btnReply.setOnClickListener {
            if (!isAlreadyLogin) {
                showShortToast(R.string.login_first)
                return@setOnClickListener
            }
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)

            CommentPopup(context).also { commentPopup ->
                this.commentPopup = commentPopup
                if (item.isChildComment) {
                    replyFragment?.apply {
                        commentPopup.setOnSendListener {
                            viewModel.postReply(
                                viewModel.csrfToken, commentId!!, commentPopup.comment
                            )
                        }
                    }
                    commentPopup.hint = context.getString(R.string.reply_child_comment)
                    commentPopup.initCommentPrefix(item.username)
                } else {
                    commentFragment?.apply {
                        commentPopup.setOnSendListener {
                            viewModel.postReply(
                                viewModel.csrfToken, item.id!!, commentPopup.comment
                            )
                        }
                    }
                    commentPopup.hint = spannable {
                        context.getString(R.string.reply).text()
                        "@${item.username}".span {
                            style(Typeface.BOLD)
                        }
                        context.getString(R.string.reply_warning).span {
                            absoluteSize(10)
                        }
                    }
                }
                XPopup.Builder(context).autoOpenSoftInput(true).asCustom(commentPopup).show()
            }
        }
    }
}