package com.yenaly.han1meviewer.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.yenaly.han1meviewer.LOCAL_DATE_TIME_FORMAT
import com.yenaly.han1meviewer.VIDEO_CODE
import com.yenaly.han1meviewer.databinding.ItemWatchHistoryBinding
import com.yenaly.han1meviewer.logic.entity.WatchHistoryEntity
import com.yenaly.han1meviewer.ui.activity.VideoActivity
import com.yenaly.yenaly_libs.utils.activity
import com.yenaly.yenaly_libs.utils.startActivity
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/26 026 15:35
 */
class WatchHistoryRvAdapter :
    BaseDifferAdapter<WatchHistoryEntity, DataBindingHolder<ItemWatchHistoryBinding>>(COMPARATOR) {

    init {
        isStateViewEnable = true
    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<WatchHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: WatchHistoryEntity,
                newItem: WatchHistoryEntity,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: WatchHistoryEntity,
                newItem: WatchHistoryEntity,
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemWatchHistoryBinding>,
        position: Int,
        item: WatchHistoryEntity?,
    ) {
        item ?: return
        holder.binding.ivCover.load(item.coverUrl) {
            crossfade(true)
        }
        holder.binding.tvAddedTime.text =
            Instant.fromEpochMilliseconds(item.watchDate).toLocalDateTime(
                TimeZone.currentSystemDefault()
            ).format(LOCAL_DATE_TIME_FORMAT)
        // 不打算顯示發佈日期，所以不用設置
        holder.binding.tvReleaseDate.text = null
        holder.binding.tvTitle.text = item.title
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): DataBindingHolder<ItemWatchHistoryBinding> {
        return DataBindingHolder(
            ItemWatchHistoryBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        ).also { viewHolder ->
            viewHolder.itemView.apply {
                setOnClickListener {
                    val position = viewHolder.bindingAdapterPosition
                    val item = getItem(position) ?: return@setOnClickListener
                    val videoCode = item.videoCode
                    context.activity?.startActivity<VideoActivity>(VIDEO_CODE to videoCode)
                }
                // setOnLongClickListener 由各自的 Fragment 实现
            }
        }
    }
}