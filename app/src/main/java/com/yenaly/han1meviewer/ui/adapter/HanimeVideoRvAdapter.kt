package com.yenaly.han1meviewer.ui.adapter

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import coil.load
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.*
import com.yenaly.han1meviewer.logic.model.HanimeInfoModel
import com.yenaly.han1meviewer.ui.activity.PreviewActivity
import com.yenaly.han1meviewer.ui.activity.SearchActivity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.copyTextToClipboard
import com.yenaly.yenaly_libs.utils.dp
import com.yenaly.yenaly_libs.utils.showShortToast
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * @project Hanime1
 * @author Yenaly Liew
 * @time 2022/06/16 016 11:04
 */
class HanimeVideoRvAdapter(private val videoWidthType: Int = -1) :  // videoWidthType is VIDEO_LAYOUT_MATCH_PARENT or VIDEO_LAYOUT_WRAP_CONTENT or nothing
    BaseMultiItemQuickAdapter<HanimeInfoModel, BaseViewHolder>() {

    init {
        addItemType(HanimeInfoModel.NORMAL, R.layout.item_hanime_video)
        addItemType(HanimeInfoModel.SIMPLIFIED, R.layout.item_hanime_video_simplified)
    }

    override fun convert(holder: BaseViewHolder, item: HanimeInfoModel) {
        when (holder.itemViewType) {
            HanimeInfoModel.SIMPLIFIED -> {
                holder.getView<ImageView>(R.id.cover).load(item.coverUrl) {
                    crossfade(true)
                }
                holder.getView<TextView>(R.id.title).text = item.title
            }
            HanimeInfoModel.NORMAL -> {
                holder.getView<TextView>(R.id.title).text = item.title
                holder.getView<ImageView>(R.id.cover).load(item.coverUrl) {
                    crossfade(true)
                }
                holder.getView<TextView>(R.id.is_playing).isVisible = item.isPlaying
                holder.getView<TextView>(R.id.duration).text = item.duration
                holder.getView<TextView>(R.id.time).apply {
                    if (item.uploadTime != null) {
                        holder.getView<View>(R.id.icon_time).isGone = false
                        text = item.uploadTime
                    } else {
                        holder.getView<View>(R.id.icon_time).isGone = true
                    }
                }
                holder.getView<TextView>(R.id.views).apply {
                    if (item.views != null) {
                        holder.getView<View>(R.id.icon_views).isGone = false
                        text = item.views
                    } else {
                        holder.getView<View>(R.id.icon_views).isGone = true
                    }
                }
                holder.getView<TextView>(R.id.genre_and_uploader).apply {
                    if (item.genre == null && item.uploader == null) {
                        isGone = true
                        return@apply
                    }
                    isGone = false
                    text = spannable {
                        item.genre.span {
                            margin(4.dp)
                            when (item.genre) {
                                "3D" -> color(Color.rgb(245, 171, 53))
                                "COS" -> color(Color.rgb(165, 55, 253))
                                "同人" -> color(Color.rgb(241, 130, 141))
                                else -> color(Color.RED)
                            }
                        }
                        item.uploader.text()
                    }
                }
            }
        }
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        when (viewType) {
            HanimeInfoModel.SIMPLIFIED -> {
                when (context) {
                    is SearchActivity -> viewHolder.getView<View>(R.id.linear).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    is VideoActivity -> when (videoWidthType) {
                        VIDEO_LAYOUT_MATCH_PARENT -> viewHolder.getView<View>(R.id.linear).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                        VIDEO_LAYOUT_WRAP_CONTENT -> viewHolder.getView<View>(R.id.linear).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                }
            }
            HanimeInfoModel.NORMAL -> {
                when (context) {
                    is VideoActivity -> when (videoWidthType) {
                        VIDEO_LAYOUT_MATCH_PARENT -> viewHolder.getView<View>(R.id.linear).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                        VIDEO_LAYOUT_WRAP_CONTENT -> viewHolder.getView<View>(R.id.linear).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                }
            }
        }
        viewHolder.itemView.apply {
            if (context !is PreviewActivity) {
                setOnClickListener {
                    val position = viewHolder.bindingAdapterPosition
                    val item = getItem(position)
                    if (item.isPlaying) {
                        // todo: strings.xml
                        showShortToast("當前正在觀看該影片哦~")
                    } else {
                        val videoCode = item.redirectLink.toVideoCode()
                        (context as Activity).startActivity<VideoActivity>(VIDEO_CODE to videoCode)
                    }
                }
                setOnLongClickListener {
                    val position = viewHolder.bindingAdapterPosition
                    val item = getItem(position)
                    copyTextToClipboard("${item.title}\n${item.redirectLink}")
                    showShortToast(context.getString(R.string.copy_to_clipboard))
                    return@setOnLongClickListener true
                }
            }
        }
    }
}