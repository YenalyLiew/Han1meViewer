package com.yenaly.han1meviewer.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.itxca.spannablex.spannable
import com.yenaly.han1meviewer.R
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.logic.entity.HKeyframeEntity
import com.yenaly.han1meviewer.logic.entity.HKeyframeHeader
import com.yenaly.han1meviewer.logic.entity.HKeyframeType
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.startActivity

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2024/04/03 003 21:40
 */
class SharedHKeyframesRvAdapter : BaseDifferAdapter<HKeyframeType, QuickViewHolder>(COMPARATOR) {
    init {
        isStateViewEnable = true
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HKeyframeType>() {
            override fun areItemsTheSame(
                oldItem: HKeyframeType,
                newItem: HKeyframeType,
            ) = when {
                oldItem is HKeyframeEntity && newItem is HKeyframeEntity -> {
                    oldItem.videoCode == newItem.videoCode
                }

                oldItem is HKeyframeHeader && newItem is HKeyframeHeader -> {
                    oldItem.title == newItem.title
                }

                else -> false
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: HKeyframeType,
                newItem: HKeyframeType,
            ) = when {
                oldItem is HKeyframeEntity && newItem is HKeyframeEntity -> {
                    oldItem == newItem
                }

                oldItem is HKeyframeHeader && newItem is HKeyframeHeader -> {
                    oldItem == newItem
                }

                else -> false

            }
        }
    }

    override fun getItemViewType(position: Int, list: List<HKeyframeType>): Int {
        return list[position].itemType
    }

    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: HKeyframeType?) {
        when (getItemViewType(position)) {
            HKeyframeType.H_KEYFRAME -> {
                require(item is HKeyframeEntity)
                holder.setText(R.id.tv_title, item.title)
                holder.getView<TextView>(R.id.tv_video_code).apply {
                    movementMethod = LinkMovementMethodCompat.getInstance()
                    text = spannable {
                        context.getString(R.string.h_keyframe_title_prefix).text()
                        item.videoCode.span {
                            clickable(color = context.getColor(R.color.video_code_link_text_color)) { _, videoCode ->
                                context.activity?.startActivity<VideoActivity>(VIDEO_CODE to videoCode)
                            }
                            underline()
                        }
                    }
                }
                holder.getView<RecyclerView>(R.id.rv_h_keyframe).apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = HKeyframeRvAdapter(item.videoCode, item).apply {
                        isLocal = item.author == null
                        isShared = true
                    }
                }
                holder.setText(R.id.tv_author, "@${item.author}")
            }

            HKeyframeType.HEADER -> {
                require(item is HKeyframeHeader)
                holder.setText(R.id.tv_title, item.title)
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return when (viewType) {
            HKeyframeType.H_KEYFRAME -> {
                QuickViewHolder(R.layout.item_shared_h_keyframes, parent)
            }

            HKeyframeType.HEADER -> {
                QuickViewHolder(R.layout.layout_header_h_keyframes, parent)
            }

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }
}