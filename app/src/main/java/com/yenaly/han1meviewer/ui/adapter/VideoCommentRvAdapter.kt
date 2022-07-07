package com.yenaly.han1meviewer.ui.adapter

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.yenaly.han1meviewer.COMMENT_ID
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.databinding.ItemVideoCommentBinding
import com.yenaly.han1meviewer.logic.model.VideoCommentModel
import com.yenaly.han1meviewer.ui.fragment.CommentReplyFragment
import com.yenaly.yenaly_libs.utils.makeBundle

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/21 021 00:03
 */
class VideoCommentRvAdapter :
    BaseQuickAdapter<VideoCommentModel, VideoCommentRvAdapter.ViewHolder>(R.layout.item_video_comment) {

    inner class ViewHolder(view: View) : BaseDataBindingHolder<ItemVideoCommentBinding>(view) {
        val binding = dataBinding!!
    }

    override fun convert(holder: ViewHolder, item: VideoCommentModel) {
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
                (CommentReplyFragment().makeBundle(COMMENT_ID to id) as CommentReplyFragment)
                    .showIn(context as FragmentActivity)
            }
        }
    }
}