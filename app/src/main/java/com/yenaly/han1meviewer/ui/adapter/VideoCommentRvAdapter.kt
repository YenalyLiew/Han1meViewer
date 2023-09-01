package com.yenaly.han1meviewer.ui.adapter

import android.graphics.Typeface
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
import com.yenaly.han1meviewer.ui.fragment.video.ChildCommentPopupFragment
import com.yenaly.han1meviewer.ui.fragment.video.CommentFragment
import com.yenaly.han1meviewer.ui.popup.ReplyPopup
import com.yenaly.yenaly_libs.utils.makeBundle
import com.yenaly.yenaly_libs.utils.showShortToast

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/21 021 00:03
 */
class VideoCommentRvAdapter constructor(private val fragment: Fragment? = null) :
    BaseQuickAdapter<VideoCommentModel.VideoComment, VideoCommentRvAdapter.ViewHolder>(R.layout.item_video_comment) {

    var replyPopup: ReplyPopup? = null

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<VideoCommentModel.VideoComment>() {
            override fun areItemsTheSame(
                oldItem: VideoCommentModel.VideoComment,
                newItem: VideoCommentModel.VideoComment,
            ): Boolean {
                return oldItem.realReplyId == newItem.realReplyId
            }

            override fun areContentsTheSame(
                oldItem: VideoCommentModel.VideoComment,
                newItem: VideoCommentModel.VideoComment,
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
        holder.binding.btnThumbUp.text = item.realLikesCount.toString()
        holder.binding.btnThumbUp.icon = if (item.post.likeCommentStatus) {
            context.getDrawable(R.drawable.ic_baseline_thumb_up_alt_24)
        } else {
            context.getDrawable(R.drawable.ic_baseline_thumb_up_off_alt_24)
        }
        holder.binding.btnThumbDown.icon = if (item.post.unlikeCommentStatus) {
            context.getDrawable(R.drawable.ic_baseline_thumb_down_alt_24)
        } else {
            context.getDrawable(R.drawable.ic_baseline_thumb_down_off_alt_24)
        }
    }

    override fun onItemViewHolderCreated(viewHolder: ViewHolder, viewType: Int) {
        viewHolder.binding.btnViewMoreReplies.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            check(fragment != null && fragment is CommentFragment)
            item.realReplyId.let { id ->
                (ChildCommentPopupFragment().makeBundle(
                    COMMENT_ID to id,
                    CSRF_TOKEN to fragment.viewModel.csrfToken
                ) as ChildCommentPopupFragment).showIn(context as FragmentActivity)
            }
        }
        viewHolder.binding.btnThumbUp.setOnClickListener {
            if (!isAlreadyLogin) {
                showShortToast(R.string.login_first)
                return@setOnClickListener
            }
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)

            if (item.isChildComment) {
                check(fragment != null && fragment is ChildCommentPopupFragment)
                fragment.viewModel.likeChildComment(
                    true, position, item,
                    likeCommentStatus = item.post.likeCommentStatus
                )
            } else {
                check(fragment != null && fragment is CommentFragment)
                fragment.viewModel.likeComment(
                    true, position, item,
                    likeCommentStatus = item.post.likeCommentStatus
                )
            }
        }
        viewHolder.binding.btnThumbDown.setOnClickListener {
            if (!isAlreadyLogin) {
                showShortToast(R.string.login_first)
                return@setOnClickListener
            }
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)
            if (item.isChildComment) {
                check(fragment != null && fragment is ChildCommentPopupFragment)
                fragment.viewModel.likeChildComment(
                    false, position, item,
                    unlikeCommentStatus = item.post.unlikeCommentStatus
                )
            } else {
                check(fragment != null && fragment is CommentFragment)
                fragment.viewModel.likeComment(
                    false, position, item,
                    unlikeCommentStatus = item.post.unlikeCommentStatus
                )
            }
        }
        viewHolder.binding.btnReply.setOnClickListener {
            if (!isAlreadyLogin) {
                showShortToast(R.string.login_first)
                return@setOnClickListener
            }
            val position = viewHolder.bindingAdapterPosition
            val item = getItem(position)

            ReplyPopup(context).also { commentPopup ->
                this.replyPopup = commentPopup
                if (item.isChildComment) {
                    check(fragment != null && fragment is ChildCommentPopupFragment)
                    fragment.apply {
                        commentPopup.setOnSendListener {
                            viewModel.postReply(
                                checkNotNull(commentId), commentPopup.comment
                            )
                        }
                    }
                    commentPopup.hint = context.getString(R.string.reply_child_comment)
                    commentPopup.initCommentPrefix(item.username)
                } else {
                    check(fragment != null && fragment is CommentFragment)
                    fragment.apply {
                        commentPopup.setOnSendListener {
                            viewModel.postReply(
                                item.realReplyId,
                                commentPopup.comment
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